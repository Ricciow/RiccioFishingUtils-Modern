package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

private data class GeneratorSpec(
    val annotation: String,
    val loaderFuncName: String,
    val methodToCall: String,
    val requiredSuperType: String? = null
)

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val specs = listOf(
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.feature.RFUFeature",
                loaderFuncName = "loadFeatures",
                methodToCall = "onInitialize",
                requiredSuperType = "cloud.glitchdev.rfu.feature.Feature"
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.events.AutoRegister",
                loaderFuncName = "registerEvents",
                methodToCall = "register",
                requiredSuperType = "cloud.glitchdev.rfu.events.RegisteredEvent"
            ),
            GeneratorSpec(
                annotation = "cloud.glitchdev.rfu.gui.components.hud.HudElement",
                loaderFuncName = "registerHud",
                methodToCall = "initialize",
                requiredSuperType = "cloud.glitchdev.rfu.gui.components.hud.AbstractHudElement"
            )
        )

        val symbolsMap = specs.associateWith { spec: GeneratorSpec ->
            resolver.getSymbolsWithAnnotation(spec.annotation)
                .filterIsInstance<KSClassDeclaration>()
                .toList()
        }

        var hasErrors = false
        symbolsMap.forEach { (spec, symbols) ->
            if (spec.requiredSuperType != null) {
                symbols.forEach { symbol ->
                    val implementsInterface = symbol.getAllSuperTypes().any { type ->
                        type.declaration.qualifiedName?.asString() == spec.requiredSuperType
                    }

                    if (!implementsInterface) {
                        logger.error(
                            "Class '${symbol.simpleName.asString()}' is annotated with @${spec.annotation.substringAfterLast(".")} " +
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

            symbolsMap.values.flatten().mapNotNull { it.qualifiedName?.asString() }.distinct().sorted().forEach {
                writer.write("import $it\n")
            }

            writer.write("\nobject $fileName {\n")

            specs.forEach { spec ->
                val symbols = symbolsMap[spec] ?: emptyList()

                writer.write("\n    fun ${spec.loaderFuncName}() {\n")
                if (symbols.isEmpty()) writer.write("        // No entries found\n")

                symbols.forEach { symbol ->
                    val name = symbol.simpleName.asString()
                    writer.write("        ")
                    if (symbol.classKind == ClassKind.OBJECT) {
                        writer.write("$name.${spec.methodToCall}()")
                    } else {
                        writer.write("$name().${spec.methodToCall}()")
                    }
                    writer.write("\n")
                }
                writer.write("    }\n")
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