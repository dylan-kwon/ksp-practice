package dylan.kwon.kotlin.ksp.myannotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HiltBinds(
    val isSingleton: Boolean = false
)