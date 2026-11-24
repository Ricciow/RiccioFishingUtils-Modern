package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.getAllSuperTypes
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
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance

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
            if (arguments.getValue("generateRegistration") == true) {
                if (packageName in generatedPackages) {
                    logger.error(
                        "Cannot generate scoped registration for '${manager.simpleName.asString()}': " +
                            "ScopedRegistrations for package '$packageName' was already generated in an earlier KSP round.",
                        manager,
                    )
                    continue
                }
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
            if (packageName in generatedPackages) {
                logger.error(
                    "Cannot generate scoped registration for '${function.simpleName.asString()}': " +
                        "ScopedRegistrations for package '$packageName' was already generated in an earlier KSP round.",
                    function,
                )
                continue
            }
            val registrations = packages.getOrPut(packageName, ::PackageRegistrations)
            val ownerName = registrations.imports.use(requireNotNull(owner.qualifiedName).asString())
            val rendered = renderScopedMethod(function, ownerName, registrations.imports) ?: continue
            registrations.sourceFiles.add(requireNotNull(owner.containingFile))
            registrations.functions.add(rendered)
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
    ): String? {
        if (Modifier.PRIVATE in function.modifiers || Modifier.PROTECTED in function.modifiers) {
            logger.error(
                "@ScopedRegistration function '${function.simpleName.asString()}' must be public or internal.",
                function,
            )
            return null
        }

        val returnType = function.returnType?.resolve()
        val tracker = when {
            returnType == null -> null
            isManagedTaskType(returnType) -> "registerTask"
            isManagedTaskPairType(returnType) -> "registerTaskPair"
            else -> null
        }
        if (returnType == null || tracker == null) {
            logger.error(
                "@ScopedRegistration function '${function.simpleName.asString()}' must return a ManagedTask " +
                    "or Pair<ManagedTask, ManagedTask>.",
                function,
            )
            return null
        }

        val parameters = function.parameters
        val validParameterNames = parameters.map { requireNotNull(it.name).asString() }.toSet()
        val defaults = readDefaults(function, validParameterNames)
        val result = renderType(returnType, imports)
        val name = function.simpleName.asString()
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
            logger.error(
                "@EventManager requires AbstractEventManager<Callback, Task> with a concrete task and a primary constructor.",
                manager,
            )
            return
        }

        val name = (arguments["name"] as? String).orEmpty().ifEmpty { "register${task.simpleName.asString()}" }
        val imports = registrations.imports
        val taskReference = imports.use(taskName)
        val validParameterNames = constructor.parameters.map { requireNotNull(it.name).asString() }.toSet()
        val defaults = readDefaults(task, validParameterNames)
        val parameters = constructor.parameters.map { parameter ->
            val parameterName = requireNotNull(parameter.name).asString()
            val reference = parameter.type
            val receiver = (reference.element as? KSCallableReference)?.receiverType?.resolve()
            val type = renderType(reference.resolve(), imports, receiver)
            val default = when {
                parameterName in defaults -> " = ${defaults.getValue(parameterName)}"
                parameterName == "priority" && type == "Int" -> {
                    val priorityDefault = arguments.getValue("defaultPriority")
                    if (priorityDefault == DEFAULT_EVENT_PRIORITY_VALUE) {
                        " = ${imports.use(DEFAULT_PRIORITY)}"
                    } else {
                        " = $priorityDefault"
                    }
                }
                parameter.hasDefault -> {
                    logger.error("Specify the default for '$parameterName' in @Defaults.", parameter)
                    ""
                }
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

    private fun readDefaults(
        declaration: KSAnnotated,
        validParameterNames: Set<String>,
    ): Map<String, String> {
        val annotation = declaration.annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == DEFAULTS
        } ?: return emptyMap()

        val result = linkedMapOf<String, String>()
        val rawEntries = (annotation.arguments.singleOrNull()?.value as? List<*>).orEmpty()
            .filterIsInstance<String>()

        for (entry in rawEntries) {
            val separatorIndex = entry.indexOf('=')
            if (separatorIndex <= 0) {
                logger.error("Invalid @Defaults entry '$entry'. Expected 'parameterName = expression'.", declaration)
                continue
            }
            val paramName = entry.substring(0, separatorIndex).trim()
            val expression = entry.substring(separatorIndex + 1).trim()
            if (paramName.isEmpty() || expression.isEmpty()) {
                logger.error("Invalid @Defaults entry '$entry'. Expected 'parameterName = expression'.", declaration)
                continue
            }
            if (paramName !in validParameterNames) {
                logger.error("Unknown parameter '$paramName' in @Defaults.", declaration)
                continue
            }
            if (paramName in result) {
                logger.error("Duplicate default for parameter '$paramName' in @Defaults.", declaration)
                continue
            }
            result[paramName] = expression
        }
        return result
    }

    private fun isManagedTaskType(type: KSType): Boolean {
        val declaration = type.declaration as? KSClassDeclaration ?: return false
        return declaration.qualifiedName?.asString() == MANAGED_TASK ||
            declaration.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == MANAGED_TASK }
    }

    private fun isManagedTaskPairType(type: KSType): Boolean {
        if (type.declaration.qualifiedName?.asString() != "kotlin.Pair") return false
        if (type.arguments.size != 2) return false
        return type.arguments.all { argument ->
            argument.type?.resolve()?.let(::isManagedTaskType) == true
        }
    }

    private fun renderType(type: KSType, imports: ImportNames, receiver: KSType? = null): String {
        val declaration = type.declaration
        if (declaration is KSTypeParameter) {
            val paramName = declaration.simpleName.asString()
            return if (type.nullability == Nullability.NULLABLE) "$paramName?" else paramName
        }

        val isFunction = type.isFunctionType || type.isSuspendFunctionType
        val name = if (isFunction) {
            val arguments = type.arguments.map { argument ->
                argument.type?.resolve()?.let { renderType(it, imports) } ?: "*"
            }
            val hasReceiver = receiver != null || type.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == EXTENSION_FUNCTION_TYPE
            }
            val suspendPrefix = if (type.isSuspendFunctionType) "suspend " else ""
            val receiverPrefix = if (hasReceiver && arguments.size >= 2) {
                val renderedReceiver = receiver?.let { renderType(it, imports) } ?: arguments.first()
                "$renderedReceiver."
            } else {
                ""
            }
            val parameters = arguments.dropLast(1).let {
                if (hasReceiver && arguments.size >= 2) it.drop(1) else it
            }
            "$suspendPrefix$receiverPrefix(${parameters.joinToString()}) -> ${arguments.last()}"
        } else {
            val qualifiedName = requireNotNull(declaration.qualifiedName).asString()
            val arguments = type.arguments.map { argument ->
                val resolved = argument.type?.resolve()
                if (resolved == null) {
                    "*"
                } else {
                    val rendered = renderType(resolved, imports)
                    when (argument.variance) {
                        Variance.COVARIANT -> "out $rendered"
                        Variance.CONTRAVARIANT -> "in $rendered"
                        Variance.STAR -> "*"
                        Variance.INVARIANT -> rendered
                    }
                }
            }
            val isDefaultImported = declaration.parentDeclaration == null &&
                declaration.packageName.asString() in DEFAULT_KOTLIN_PACKAGES
            val simpleName = if (isDefaultImported) {
                declaration.simpleName.asString()
            } else {
                imports.use(qualifiedName)
            }
            if (arguments.isEmpty()) simpleName else "$simpleName<${arguments.joinToString()}>"
        }
        if (type.nullability != Nullability.NULLABLE) return name
        return if (isFunction) "($name)?" else "$name?"
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
        const val MANAGED_TASK = "cloud.glitchdev.rfu.events.AbstractEventManager.ManagedTask"
        const val DEFAULT_PRIORITY = "cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY"
        const val DEFAULT_EVENT_PRIORITY_VALUE = 20
        const val SCOPE = "cloud.glitchdev.rfu.events.EventScope"
        const val EXTENSION_FUNCTION_TYPE = "kotlin.ExtensionFunctionType"

        val DEFAULT_KOTLIN_PACKAGES = setOf(
            "kotlin",
            "kotlin.annotation",
            "kotlin.collections",
            "kotlin.comparisons",
            "kotlin.io",
            "kotlin.jvm",
            "kotlin.ranges",
            "kotlin.sequences",
            "kotlin.text",
        )
    }
}
