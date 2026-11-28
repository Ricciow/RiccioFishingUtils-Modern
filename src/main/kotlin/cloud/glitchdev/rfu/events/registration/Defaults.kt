package cloud.glitchdev.rfu.events.registration

/** Supplies Kotlin defaults as `"parameter = expression"` entries for a generated registration. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Defaults(vararg val values: String)
