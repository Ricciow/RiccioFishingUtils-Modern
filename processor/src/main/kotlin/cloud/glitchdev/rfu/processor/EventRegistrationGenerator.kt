package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability

internal class EventRegistrationGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    private val generated = mutableSetOf<String>()

    fun generate(resolver: Resolver): List<KSClassDeclaration> = buildList {
        for (manager in resolver.getSymbolsWithAnnotation(ANNOTATION).filterIsInstance<KSClassDeclaration>()) {
            val annotation = manager.annotations.single {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION
            }
            val arguments = annotation.arguments.associate { it.name?.asString() to it.value }

            if (arguments.getValue("register") == true) add(manager)

            val managerName = requireNotNull(manager.qualifiedName).asString()
            if (generated.add(managerName)) {
                generateRegistration(manager, arguments)
            }
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
        val parameters = constructor.parameters.map { parameter ->
            val parameterName = requireNotNull(parameter.name).asString()
            val type = renderType(parameter.type.resolve(), imports)
            val longDefault = parameter.annotations.firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == DEFAULT_LONG
            }?.arguments?.single()?.value as? Long
            val default = when {
                longDefault != null -> " = ${longDefault}L"
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
                writer.appendLine("): $simpleName = registerTask($simpleName($values))")
            }
    }

    private fun renderType(type: KSType, imports: MutableSet<String>): String {
        val qualifiedName = requireNotNull(type.declaration.qualifiedName).asString()
        val arguments = type.arguments.map { argument ->
            argument.type?.resolve()?.let { renderType(it, imports) } ?: "*"
        }
        val name = if (type.isFunctionType) {
            "(${arguments.dropLast(1).joinToString()}) -> ${arguments.last()}"
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
        const val ABSTRACT_EVENT_MANAGER = "cloud.glitchdev.rfu.events.AbstractEventManager"
        const val DEFAULT_LONG = "cloud.glitchdev.rfu.events.registration.DefaultLong"
        const val SCOPE = "cloud.glitchdev.rfu.events.EventScope"
    }
}
