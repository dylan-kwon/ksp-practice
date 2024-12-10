@file:OptIn(KspExperimental::class)

package dylan.kwon.kotlin.ksp.myprocessor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
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

    private val visitor = Visitor()

    override fun process(resolver: Resolver): List<KSAnnotated> {

        // HiltBinds 애너테이션이 사용된 심볼 검색
        val symbols = resolver.getSymbolsWithAnnotation(
            HiltBinds::class.java.canonicalName
        )

        // 검증 여부로 그룹핑
        val group = symbols.groupBy {
            it.validate()
        }

        // 검증에 성공한 심볼만 처리
        group.getOrDefault(true, emptyList()).forEach { declaration ->
            declaration.accept(visitor, Unit)
        }

        // 검증에 실패한 심볼은 다음 라운드에서 처리
        return group.getOrDefault(false, emptyList())
    }

    private inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {

            // 애너테이션 정보
            val annotation = classDeclaration.getAnnotationsByType(HiltBinds::class).first()
            val annotationName = HiltBinds::class.java.simpleName

            // 애너테이션이 사용된 클래스의 패키지, 이름 등 조회
            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.simpleName.asString()

            // 자동 생성될 파일 이름 지정
            val newFileName = "Generate_${annotationName}_${className}Module"

            // 애너테이션이 사용된 클래스의 부모 타입의 이름 조회
            // 이 포스트의 예시에서는 Repository Interface를 지칭함
            val superClassName =
                classDeclaration.superTypes.first().resolve().declaration.simpleName.asString()

            // 애너테이션이 사용된 클래스가 변경되면 재구축하도록 의존성 설정
            // 또한 aggregating를 true로 설정하여 부모 타입의 변경에도 재구축하도록 설정
            val dependencies = Dependencies(
                aggregating = true, classDeclaration.containingFile!!
            )

            // 코드 생성을 위한 outputStream 생성
            val outputStream = codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = packageName,
                fileName = newFileName
            )

            // 자동 생성될 코드 내용 작성 - 시작
            PrintStream(outputStream).use {
                it.println("package ${classDeclaration.packageName.asString()}")

                it.println()

                it.println("import dagger.Binds")
                it.println("import dagger.Module")
                it.println("import dagger.hilt.InstallIn")
                it.println("import dagger.hilt.components.SingletonComponent")

                // isSingleton = true인 경우에만 추가
                if (annotation.isSingleton) {
                    it.println("import javax.inject.Singleton")
                }

                it.println()

                it.println("@Module")
                it.println("@InstallIn(SingletonComponent::class)")
                it.println("abstract class $newFileName {")
                it.println("\t@Binds")

                // isSingleton = true인 경우에만 추가
                if (annotation.isSingleton) {
                    it.println("\t@Singleton")
                }

                it.println("\tabstract fun binds$className(")
                it.println("\t\t${className.lowercase()}: $className")
                it.println("\t): $superClassName")
                it.println("}")
            }
            // 자동 생성될 코드 내용 작성 - 종료
        }
    }
}