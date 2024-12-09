package dylan.kwon.kotlin.ksp.myprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import dylan.kwon.kotlin.ksp.myannotation.HiltBinds
import java.io.PrintStream

class HiltBindsSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return HiltBindsSymbolProcessor(environment.codeGenerator)
    }
}

class HiltBindsSymbolProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationName = HiltBinds::class.java.simpleName
        val symbols = resolver.getSymbolsWithAnnotation(
            HiltBinds::class.java.canonicalName
        )
        val group = symbols.groupBy {
            it.validate()
        }
        group.getOrDefault(true, emptyList())
            .filterIsInstance<KSClassDeclaration>()
            .forEach { declaration ->

                val packageName = declaration.packageName.asString()
                val className = declaration.simpleName.asString()
                val newFileName = "Gen_${annotationName}_${className}"

                val superClassName =
                    declaration.superTypes.first().resolve().declaration.simpleName.asString()

                val dependencies = Dependencies(
                    aggregating = true, declaration.containingFile!!
                )
                val outputStream = codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = packageName,
                    fileName = newFileName
                )
                PrintStream(outputStream).use {

                    it.println("package ${declaration.packageName.asString()}")

                    it.println()

                    it.println("import dagger.Binds")
                    it.println("import dagger.Module")
                    it.println("import dagger.hilt.InstallIn")
                    it.println("import dagger.hilt.components.SingletonComponent")

                    it.println()

                    it.println("@Module")
                    it.println("@InstallIn(SingletonComponent::class)")
                    it.println("abstract class $newFileName {")
                    it.println("\t@Binds")
                    it.println("\tabstract fun binds$className(")
                    it.println("\t\t${className.lowercase()}: $className")
                    it.println("\t): $superClassName")
                    it.println("}")
                }
            }
        return group.getOrDefault(false, emptyList())
    }
}