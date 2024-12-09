package dylan.kwon.kotlin.ksp.myprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dylan.kwon.kotlin.ksp.myannotation.MyAnnotation

class MyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyProcessor(environment.options, environment.codeGenerator)
    }
}

class MyProcessor(
    private val options: Map<String, String>,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(MyAnnotation::class.java.canonicalName).forEach {

            val declarationClass = it as KSClassDeclaration

            // 종속성
            val dependencies = Dependencies(
                aggregating = true, declarationClass.containingFile!!
            )

            val outputName = "Gen_${declarationClass.simpleName.asString()}_${options["option1"]}"
            val output = codeGenerator.createNewFile(
                dependencies, "dylan.kwon.generated", outputName
            )
            output.bufferedWriter().use { writer ->
                writer.write("class Haha")
            }
        }
        return emptyList()
    }
}

