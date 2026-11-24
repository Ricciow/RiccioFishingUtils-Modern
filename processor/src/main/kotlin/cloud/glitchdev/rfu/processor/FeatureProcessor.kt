package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

private const val AUTO_REGISTER = "cloud.glitchdev.rfu.events.AutoRegister"
private const val RFU_FEATURE = "cloud.glitchdev.rfu.feature.RFUFeature"
private const val HUD_ELEMENT = "cloud.glitchdev.rfu.gui.hud.HudElement"

private data class LoaderScope(val fieldName: String, val unloadFuncName: String)

private data class GeneratorSpec(
    val annotation: String,
    val loaderFuncName: String,
    val methodToCall: String,
    val requiredSuperType: String? = null,
    val scope: LoaderScope? = null,
)

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val eventRegistrations = EventRegistrationGenerator(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val autoRegisteredManagers = eventRegistrations.generate(resolver)
        val specs = listOf(
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.achievement.Achievement",
                loaderFuncName = "registerAchievements",
                methodToCall = "init",
                requiredSuperType = "cloud.glitchdev.rfu.achievement.BaseAchievement"
            ),
            GeneratorSpec(
                annotation = RFU_FEATURE,
                loaderFuncName = "loadFeatures",
                methodToCall = "onInitialize",
                requiredSuperType = "cloud.glitchdev.rfu.feature.Feature",
                scope = LoaderScope("featureScopes", "unloadFeatures"),
            ),
            GeneratorSpec(
                annotation = AUTO_REGISTER,
                loaderFuncName = "registerEvents",
                methodToCall = "register",
                requiredSuperType = "cloud.glitchdev.rfu.events.RegisteredEvent"
            ),
            GeneratorSpec(
                annotation = AUTO_REGISTER,
                loaderFuncName = "postInitializeEvents",
                methodToCall = "postInitialize",
                requiredSuperType = "cloud.glitchdev.rfu.events.RegisteredEvent"
            ),
            GeneratorSpec(
                annotation = HUD_ELEMENT,
                loaderFuncName = "registerHud",
                methodToCall = "initialize",
                requiredSuperType = "cloud.glitchdev.rfu.gui.hud.AbstractHudElement",
                scope = LoaderScope("hudScopes", "unloadHud"),
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.utils.command.Command",
                loaderFuncName = "registerCommands",
                methodToCall = "register",
                requiredSuperType = "cloud.glitchdev.rfu.utils.command.AbstractCommand"
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.events.InstantRegister",
                loaderFuncName = "registerInstantEvents",
                methodToCall = "instantRegister",
                requiredSuperType = "cloud.glitchdev.rfu.events.InstantRegisteredEvent"
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.party.PartyCommand",
                loaderFuncName = "registerPartyCommands",
                methodToCall = "init",
                requiredSuperType = "cloud.glitchdev.rfu.party.IPartyCommand"
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge",
                loaderFuncName = "registerChallenges",
                methodToCall = "register",
                requiredSuperType = "cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge"
            )
        )

        val symbolsMap = specs.associateWith { spec: GeneratorSpec ->
            val symbols = resolver.getSymbolsWithAnnotation(spec.annotation)
                .filterIsInstance<KSClassDeclaration>()
                .toList()
            if (spec.annotation == AUTO_REGISTER) {
                (symbols + autoRegisteredManagers)
                    .distinctBy { it.qualifiedName?.asString() }
                    .sortedWith(compareBy<KSClassDeclaration, String>(String.CASE_INSENSITIVE_ORDER) {
                        it.containingFile?.filePath.orEmpty()
                    }.thenBy { (it.location as? FileLocation)?.lineNumber ?: 0 })
            } else {
                symbols
            }
        }

        var hasErrors = false
        symbolsMap.forEach { (spec, symbols) ->
            if (spec.requiredSuperType != null) {
                symbols.forEach { symbol ->
                    val implementsInterface = symbol.getAllSuperTypes().any { type ->
                        type.declaration.qualifiedName?.asString() == spec.requiredSuperType
                    }

                    if (!implementsInterface) {
                        val annotation = if (symbol in autoRegisteredManagers) "EventManager" else spec.annotation.substringAfterLast(".")
                        logger.error(
                            "Class '${symbol.simpleName.asString()}' is annotated with @$annotation " +
                                    "but does not implement '${spec.requiredSuperType}'.",
                            symbol
                        )
                        hasErrors = true
                    }
                }
            }
        }

        if (hasErrors) return emptyList()
        if (symbolsMap.values.all { it.isEmpty() }) return emptyList()

        val packageName = "cloud.glitchdev.rfu.generated"
        val fileName = "RFULoader"
        val allFiles = symbolsMap.values.flatten().mapNotNull { it.containingFile }.toTypedArray()

        val file: OutputStream = codeGenerator.createNewFile(
            Dependencies(true, *allFiles), packageName, fileName
        )

        file.bufferedWriter().use { writer ->
            writer.write("package $packageName\n\n")

            val allSymbols = symbolsMap.values.flatten()
            writer.write("import cloud.glitchdev.rfu.events.EventScopes\n")
            writer.write("import cloud.glitchdev.rfu.gui.window.HudWindow\n")

            // Detect duplicate simple names so we can use fully-qualified references for them
            val simpleNameCounts = allSymbols
                .groupBy { it.simpleName.asString() }
                .mapValues { it.value.size }

            // Only emit imports for symbols whose simple name is unique
            allSymbols
                .mapNotNull { it.qualifiedName?.asString() }
                .distinct()
                .filter { fqn -> simpleNameCounts[fqn.substringAfterLast(".")] == 1 }
                .sorted()
                .forEach { writer.write("import $it\n") }

            writer.write("\nobject $fileName {\n")
            specs.mapNotNull { it.scope }.forEach {
                writer.write("    private val ${it.fieldName} = EventScopes()\n")
            }

            specs.forEach { spec ->
                val symbols = symbolsMap[spec] ?: emptyList()
                val scope = spec.scope

                writer.write("\n    fun ${spec.loaderFuncName}() {\n")
                if (scope != null) writer.write("        ${scope.fieldName}.load { scope ->\n")
                val indent = if (scope != null) "            " else "        "
                if (spec.annotation == HUD_ELEMENT) writer.write("${indent}HudWindow.initialize(scope.child())\n")
                if (symbols.isEmpty()) writer.write("$indent// No entries found\n")

                symbols.forEach { symbol ->
                    val simpleName = symbol.simpleName.asString()
                    // Use fully-qualified name when simple name collides with another symbol
                    val ref = if ((simpleNameCounts[simpleName] ?: 0) > 1) {
                        symbol.qualifiedName?.asString() ?: simpleName
                    } else {
                        simpleName
                    }
                    val instance = if (symbol.classKind == ClassKind.OBJECT) ref else "$ref()"
                    val arguments = if (scope != null) "scope.child()" else ""
                    writer.write("$indent$instance.${spec.methodToCall}($arguments)\n")
                }
                if (scope != null) writer.write("        }\n")
                writer.write("    }\n")
                if (scope != null) writer.write("\n    fun ${scope.unloadFuncName}() = ${scope.fieldName}.close()\n")
            }
            writer.write("}")
        }

        logger.info("RFU Loader generated successfully.")
        return emptyList()
    }
}

class FeatureProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FeatureProcessor(environment.codeGenerator, environment.logger)
    }
}
