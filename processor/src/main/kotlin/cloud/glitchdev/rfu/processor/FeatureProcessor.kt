package cloud.glitchdev.rfu.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream
import java.nio.file.FileAlreadyExistsException

class FeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationName = "cloud.glitchdev.rfu.feature.RFUFeature"

        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        val resourceFile = "META-INF/services/cloud.glitchdev.rfu.feature.Feature"

        try {
            val file: OutputStream = codeGenerator.createNewFile(
                Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
                "",
                resourceFile,
                ""
            )

            file.bufferedWriter().use { writer ->
                symbols.forEach { classDeclaration ->
                    val className = classDeclaration.qualifiedName?.asString()
                    if (className != null) {
                        writer.write(className)
                        writer.newLine()
                    }
                }
            }
            logger.warn("Registered ${symbols.size} features for $annotationName")
        } catch (e: FileAlreadyExistsException) {
            //Just a catch
        }

        return emptyList()
    }
}

class FeatureProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FeatureProcessor(environment.codeGenerator, environment.logger)
    }
}