package dylan.kwon.kotlin.ksp.myprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dylan.kwon.kotlin.ksp.myannotation.MyAnnotation

class MyProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyProcessor(environment.codeGenerator)
    }
}

class MyProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(MyAnnotation::class.java.canonicalName).forEach {
            val declarationAClass = it as KSClassDeclaration
            val declarationBClass = declarationAClass.superTypes.first().resolve().declaration

            val dependencies = Dependencies(aggregating = true, declarationAClass.containingFile!!)

            val outputName = "Gen_${declarationBClass.simpleName.asString()}"
            val output =
                codeGenerator.createNewFile(dependencies, "dylan.kwon.generated", outputName)

            output.bufferedWriter().use {
                it.write("class Haha")
            }
        }
        return emptyList()
    }
}

fun KSClassDeclaration.getDeclaredFunctions(): Sequence<KSFunctionDeclaration> =
    declarations.filterIsInstance<KSFunctionDeclaration>()