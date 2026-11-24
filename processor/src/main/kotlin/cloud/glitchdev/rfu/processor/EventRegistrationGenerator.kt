package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability

internal class EventRegistrationGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    private val generated = mutableSetOf<String>()

    fun generate(resolver: Resolver): List<KSClassDeclaration> = buildList {
        generateScopedMethods(resolver)
        for (manager in resolver.getSymbolsWithAnnotation(ANNOTATION).filterIsInstance<KSClassDeclaration>()) {
            val annotation = manager.annotations.single {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION
            }
            val arguments = annotation.arguments.associate { it.name?.asString() to it.value }

            if (arguments.getValue("register") == true) add(manager)

            val managerName = requireNotNull(manager.qualifiedName).asString()
            if (arguments.getValue("generateRegistration") == true && generated.add(managerName)) {
                generateRegistration(manager, arguments)
            }
        }
    }

    private fun generateScopedMethods(resolver: Resolver) {
        val methods = resolver.getSymbolsWithAnnotation(SCOPED_REGISTRATION)
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { function ->
                val owner = function.parentDeclaration as? KSClassDeclaration
                if (owner?.classKind != ClassKind.OBJECT) {
                    logger.error("@ScopedRegistration requires a method on a Kotlin object.", function)
                    null
                } else {
                    owner to function
                }
            }
            .groupBy({ it.first }, { it.second })

        for ((owner, functions) in methods) {
            val ownerName = requireNotNull(owner.qualifiedName).asString()
            val packageName = owner.packageName.asString()
            val fileName = "${ownerName.removePrefix("$packageName.").replace('.', '_')}ScopedRegistrations"
            if (!generated.add("$packageName.$fileName")) continue
            val imports = sortedSetOf(SCOPE, ownerName)
            val registrations = functions.map { renderScopedMethod(it, owner.simpleName.asString(), imports) }
            codeGenerator.createNewFile(Dependencies(false, requireNotNull(owner.containingFile)), packageName, fileName)
                .bufferedWriter().use { writer ->
                    writer.appendLine("package $packageName")
                    writer.appendLine()
                    imports.forEach { writer.appendLine("import $it") }
                    registrations.forEach { writer.appendLine().appendLine(it) }
                }
        }
    }

    private fun renderScopedMethod(
        function: KSFunctionDeclaration,
        ownerName: String,
        imports: MutableSet<String>,
    ): String {
        val defaults = readDefaults(function)
        val returnType = requireNotNull(function.returnType).resolve()
        val result = renderType(returnType, imports)
        val tracker = if (returnType.declaration.qualifiedName?.asString() == "kotlin.Pair") {
            "registerTaskPair"
        } else {
            "registerTask"
        }
        val name = function.simpleName.asString()
        val parameters = function.parameters
        val arguments = parameters.joinToString { requireNotNull(it.name).asString() }
        return buildString {
            appendLine("fun EventScope.$name(")
            for (parameter in parameters) {
                val parameterName = requireNotNull(parameter.name).asString()
                val reference = parameter.type
                val receiver = (reference.element as? KSCallableReference)?.receiverType?.resolve()
                val type = renderType(reference.resolve(), imports, receiver)
                val default = when {
                    !parameter.hasDefault -> ""
                    parameterName in defaults -> " = ${defaults.getValue(parameterName)}"
                    parameterName == "priority" && type == "Int" -> {
                        imports.add(DEFAULT_PRIORITY)
                        " = DEFAULT_EVENT_PRIORITY"
                    }
                    else -> {
                        logger.error("Specify the default for '$parameterName' in @Defaults.", parameter)
                        ""
                    }
                }
                appendLine("    $parameterName: $type$default,")
            }
            append("): $result = $tracker { $ownerName.$name($arguments) }")
        }
    }

    private fun generateRegistration(manager: KSClassDeclaration, arguments: Map<String?, Any?>) {
        val managerType = manager.superTypes.map { it.resolve() }.firstOrNull {
            it.declaration.qualifiedName?.asString() == ABSTRACT_EVENT_MANAGER
        }
        val task = managerType?.arguments?.getOrNull(1)?.type?.resolve()?.declaration as? KSClassDeclaration
        val constructor = task?.primaryConstructor
        val taskName = task?.qualifiedName?.asString()
        if (task == null || constructor == null || taskName == null) {
            logger.error("@EventManager requires AbstractEventManager<Callback, Task> with a concrete task and a primary constructor.", manager)
            return
        }

        val simpleName = task.simpleName.asString()
        val name = (arguments["name"] as? String).orEmpty().ifEmpty { "register$simpleName" }
        val imports = sortedSetOf(SCOPE, taskName)
        val defaults = readDefaults(task)
        val parameters = constructor.parameters.map { parameter ->
            val parameterName = requireNotNull(parameter.name).asString()
            val type = renderType(parameter.type.resolve(), imports)
            val default = when {
                parameterName in defaults -> " = ${defaults.getValue(parameterName)}"
                parameterName == "priority" && type == "Int" -> " = ${arguments.getValue("defaultPriority")}"
                else -> ""
            }
            "$parameterName: $type$default"
        }
        val values = constructor.parameters.joinToString { requireNotNull(it.name).asString() }
        val packageName = manager.packageName.asString()
        val fileName = "${taskName.removePrefix("${task.packageName.asString()}.").replace('.', '_')}Registration"
        val sourceFiles = listOfNotNull(manager.containingFile, task.containingFile).distinct().toTypedArray()
        codeGenerator.createNewFile(Dependencies(false, *sourceFiles), packageName, fileName)
            .bufferedWriter().use { writer ->
                writer.appendLine("package $packageName")
                writer.appendLine()
                imports.forEach { writer.appendLine("import $it") }
                writer.appendLine()
                writer.appendLine("fun EventScope.$name(")
                parameters.forEach { writer.appendLine("    $it,") }
                writer.appendLine("): $simpleName = registerTask { $simpleName($values).register() }")
            }
    }

    private fun readDefaults(declaration: KSAnnotated): Map<String, String> {
        val annotation = declaration.annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DEFAULTS
        }
        return (annotation?.arguments?.singleOrNull()?.value as? List<*>).orEmpty()
            .filterIsInstance<String>()
            .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
    }

    private fun renderType(type: KSType, imports: MutableSet<String>, receiver: KSType? = null): String {
        val qualifiedName = requireNotNull(type.declaration.qualifiedName).asString()
        val arguments = type.arguments.map { argument ->
            argument.type?.resolve()?.let { renderType(it, imports) } ?: "*"
        }
        val name = if (type.isFunctionType) {
            val receiverPrefix = receiver?.let { "${renderType(it, imports)}." }.orEmpty()
            val parameters = arguments.dropLast(1).let { if (receiver == null) it else it.drop(1) }
            "$receiverPrefix(${parameters.joinToString()}) -> ${arguments.last()}"
        } else {
            if (!qualifiedName.startsWith("kotlin.")) imports.add(qualifiedName)
            val simpleName = qualifiedName.substringAfterLast('.')
            if (arguments.isEmpty()) simpleName else "$simpleName<${arguments.joinToString()}>"
        }
        if (type.nullability != Nullability.NULLABLE) return name
        return if (type.isFunctionType) "($name)?" else "$name?"
    }

    private companion object {
        const val ANNOTATION = "cloud.glitchdev.rfu.events.EventManager"
        const val SCOPED_REGISTRATION = "cloud.glitchdev.rfu.events.registration.ScopedRegistration"
        const val DEFAULTS = "cloud.glitchdev.rfu.events.registration.Defaults"
        const val ABSTRACT_EVENT_MANAGER = "cloud.glitchdev.rfu.events.AbstractEventManager"
        const val DEFAULT_PRIORITY = "cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY"
        const val SCOPE = "cloud.glitchdev.rfu.events.EventScope"
    }
}
