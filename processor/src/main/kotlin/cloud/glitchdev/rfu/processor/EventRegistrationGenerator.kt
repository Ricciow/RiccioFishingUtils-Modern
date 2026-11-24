package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability

internal class EventRegistrationGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    private val generatedPackages = mutableSetOf<String>()

    fun generate(resolver: Resolver): List<KSClassDeclaration> = buildList {
        val packages = sortedMapOf<String, PackageRegistrations>()
        val managers = resolver.getSymbolsWithAnnotation(ANNOTATION).filterIsInstance<KSClassDeclaration>()
            .sortedBy { it.qualifiedName?.asString() }
        for (manager in managers) {
            val annotation = manager.annotations.single {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION
            }
            val arguments = annotation.arguments.associate { it.name?.asString() to it.value }

            if (arguments.getValue("register") == true) add(manager)

            val packageName = manager.packageName.asString()
            if (arguments.getValue("generateRegistration") == true && packageName !in generatedPackages) {
                val registrations = packages.getOrPut(packageName, ::PackageRegistrations)
                collectTaskRegistration(manager, arguments, registrations)
            }
        }
        val methods = resolver.getSymbolsWithAnnotation(SCOPED_REGISTRATION)
            .filterIsInstance<KSFunctionDeclaration>()
            .sortedBy { it.qualifiedName?.asString() }
        for (function in methods) {
            val owner = function.parentDeclaration as? KSClassDeclaration
            if (owner?.classKind != ClassKind.OBJECT) {
                logger.error("@ScopedRegistration requires a method on a Kotlin object.", function)
                continue
            }
            val packageName = owner.packageName.asString()
            if (packageName in generatedPackages) continue
            val registrations = packages.getOrPut(packageName, ::PackageRegistrations)
            val ownerName = registrations.imports.use(requireNotNull(owner.qualifiedName).asString())
            registrations.sourceFiles.add(requireNotNull(owner.containingFile))
            registrations.functions.add(renderScopedMethod(function, ownerName, registrations.imports))
        }
        for ((packageName, registrations) in packages) {
            if (registrations.functions.isEmpty()) continue
            val dependencies = Dependencies(true, *registrations.sourceFiles.toTypedArray())
            codeGenerator.createNewFile(dependencies, packageName, "ScopedRegistrations")
                .bufferedWriter().use { writer ->
                    writer.appendLine("package $packageName")
                    writer.appendLine()
                    registrations.imports.directives().forEach { writer.appendLine("import $it") }
                    registrations.functions.forEach { writer.appendLine().appendLine(it) }
                }
            generatedPackages.add(packageName)
        }
    }

    private fun renderScopedMethod(
        function: KSFunctionDeclaration,
        ownerName: String,
        imports: ImportNames,
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
                    parameterName == "priority" && type == "Int" -> " = ${imports.use(DEFAULT_PRIORITY)}"
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

    private fun collectTaskRegistration(
        manager: KSClassDeclaration,
        arguments: Map<String?, Any?>,
        registrations: PackageRegistrations,
    ) {
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

        val name = (arguments["name"] as? String).orEmpty().ifEmpty { "register${task.simpleName.asString()}" }
        val imports = registrations.imports
        val taskReference = imports.use(taskName)
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
        registrations.sourceFiles.addAll(listOfNotNull(manager.containingFile, task.containingFile))
        registrations.functions.add(buildString {
            appendLine("fun EventScope.$name(")
            parameters.forEach { appendLine("    $it,") }
            append("): $taskReference = registerTask { $taskReference($values).register() }")
        })
    }

    private fun readDefaults(declaration: KSAnnotated): Map<String, String> {
        val annotation = declaration.annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DEFAULTS
        }
        return (annotation?.arguments?.singleOrNull()?.value as? List<*>).orEmpty()
            .filterIsInstance<String>()
            .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
    }

    private fun renderType(type: KSType, imports: ImportNames, receiver: KSType? = null): String {
        val qualifiedName = requireNotNull(type.declaration.qualifiedName).asString()
        val arguments = type.arguments.map { argument ->
            argument.type?.resolve()?.let { renderType(it, imports) } ?: "*"
        }
        val name = if (type.isFunctionType) {
            val receiverPrefix = receiver?.let { "${renderType(it, imports)}." }.orEmpty()
            val parameters = arguments.dropLast(1).let { if (receiver == null) it else it.drop(1) }
            "$receiverPrefix(${parameters.joinToString()}) -> ${arguments.last()}"
        } else {
            val simpleName = if (qualifiedName.startsWith("kotlin.")) {
                qualifiedName.substringAfterLast('.')
            } else {
                imports.use(qualifiedName)
            }
            if (arguments.isEmpty()) simpleName else "$simpleName<${arguments.joinToString()}>"
        }
        if (type.nullability != Nullability.NULLABLE) return name
        return if (type.isFunctionType) "($name)?" else "$name?"
    }

    private class PackageRegistrations {
        val sourceFiles = mutableSetOf<KSFile>()
        val imports = ImportNames().apply { use(SCOPE) }
        val functions = mutableListOf<String>()
    }

    private class ImportNames {
        private val namesByImport = sortedMapOf<String, String>()
        private val usedNames = mutableSetOf<String>()

        fun use(qualifiedName: String): String = namesByImport.getOrPut(qualifiedName) {
            val simpleName = qualifiedName.substringAfterLast('.')
            if (usedNames.add(simpleName)) return@getOrPut simpleName

            val qualifier = qualifiedName.substringBeforeLast('.').substringAfterLast('.')
            val alias = qualifier.replaceFirstChar(Char::uppercaseChar) + simpleName
            var name = alias
            var suffix = 2
            while (!usedNames.add(name)) name = "$alias${suffix++}"
            name
        }

        fun directives(): List<String> = namesByImport.map { (qualifiedName, name) ->
            if (qualifiedName.substringAfterLast('.') == name) qualifiedName else "$qualifiedName as $name"
        }
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
