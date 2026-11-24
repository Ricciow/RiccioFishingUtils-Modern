package cloud.glitchdev.rfu.feature

import cloud.glitchdev.rfu.events.EventScope

abstract class ScopedFeature : Feature {
    private var events: EventScope? = null

    final override fun onInitialize() {
        stop()
        val scope = EventScope()
        try {
            onInitialize(scope)
            events = scope
        } catch (failure: Throwable) {
            try {
                scope.close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }

    fun stop() {
        val scope = events
        events = null
        scope?.close()
    }

    protected abstract fun onInitialize(scope: EventScope)
}
