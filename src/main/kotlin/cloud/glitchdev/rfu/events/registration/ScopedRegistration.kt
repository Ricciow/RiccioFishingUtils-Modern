package cloud.glitchdev.rfu.events.registration

/** Generates a scoped extension that delegates to an existing registration method. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ScopedRegistration
