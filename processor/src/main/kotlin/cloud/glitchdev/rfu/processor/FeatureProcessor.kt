package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

private data class GeneratorSpec(
    val annotation: String,
    val loaderFuncName: String,
    val methodToCall: String
)

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // --- CONFIGURATION: Annotation stuff ---
        val specs = listOf(
            GeneratorSpec("cloud.glitchdev.rfu.feature.RFUFeature", "loadFeatures", "onInitialize"),
            GeneratorSpec("cloud.glitchdev.rfu.events.AutoRegister", "registerEvents", "register")
        )

        val symbolsMap = specs.associateWith { spec ->
            resolver.getSymbolsWithAnnotation(spec.annotation)
                .filterIsInstance<KSClassDeclaration>()
                .toList()
        }

        if (symbolsMap.values.all { it.isEmpty() }) return emptyList()

        val packageName = "cloud.glitchdev.rfu.generated"
        val fileName = "RFULoader"
        val allFiles = symbolsMap.values.flatten().mapNotNull { it.containingFile }.toTypedArray()

        val file: OutputStream = codeGenerator.createNewFile(
            Dependencies(true, *allFiles), packageName, fileName
        )

        file.bufferedWriter().use { writer ->
            writer.write("package $packageName\n\n")
            symbolsMap.values.flatten().distinct().forEach {
                writer.write("import ${it.qualifiedName!!.asString()}\n")
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

        logger.warn("RFU Loader generated successfully.")
        return emptyList()
    }
}

class FeatureProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FeatureProcessor(environment.codeGenerator, environment.logger)
    }
}