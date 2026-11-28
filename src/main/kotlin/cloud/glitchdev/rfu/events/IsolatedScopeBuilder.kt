package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.utils.RFULogger

internal class IsolatedScopeBuilder(private val parent: EventScope) {
    internal var failure: Throwable? = null
        private set

    fun mount(name: String, initialize: (EventScope) -> Unit) {
        val child = parent.child()
        try {
            initialize(child)
        } catch (error: Throwable) {
            try {
                child.close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== error) error.addSuppressed(cleanupFailure)
            }
            RFULogger.error("Failed to initialize $name", error)
            val first = failure
            if (first == null) failure = error
            else if (first !== error) first.addSuppressed(error)
        }
    }
}
