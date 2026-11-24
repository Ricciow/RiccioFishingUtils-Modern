package cloud.glitchdev.rfu.events

internal class EventScopes {
    private var activeScope: EventScope? = null

    fun load(initialize: (EventScope) -> Unit) {
        if (activeScope != null) return
        val scope = EventScope()
        activeScope = scope
        try {
            initialize(scope)
        } catch (failure: Throwable) {
            activeScope = null
            try {
                scope.close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }

    fun close() {
        val scope = activeScope
        activeScope = null
        scope?.close()
    }
}
