package cloud.glitchdev.rfu.events

internal class EventScopes {
    var activeScope: EventScope? = null
        private set

    fun load(initialize: (EventScope) -> Unit): EventScope {
        activeScope?.let { return it }
        owners[this] = initialize
        return mount(initialize)
    }

    private fun mount(initialize: (EventScope) -> Unit): EventScope {
        val scope = try {
            restartableScope.child()
        } catch (failure: Throwable) {
            owners.remove(this)
            throw failure
        }
        activeScope = scope
        scope.onClose { activeScope = null }
        try {
            initialize(scope)
        } catch (failure: Throwable) {
            try {
                close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
        return scope
    }

    fun close() {
        owners.remove(this)
        val scope = activeScope
        activeScope = null
        scope?.close()
    }

    companion object {
        private val clientScope = EventScope()
        /** Owns startup listeners until the client shuts down. */
        val startup = clientScope.child()
        private var restartableScope = clientScope.child()
        private val owners = linkedMapOf<EventScopes, (EventScope) -> Unit>()

        fun restart() {
            val previousOwners = owners.keys.toList()
            val closeRoot: () -> Unit = {
                try {
                    restartableScope.close()
                } finally {
                    restartableScope = clientScope.child()
                }
            }
            val restoreOwners: List<() -> Unit> = previousOwners.map { owner ->
                { owners[owner]?.let { owner.load(it) } }
            }
            runScopeActions(listOf(closeRoot) + restoreOwners)
        }

        fun shutdown() {
            owners.clear()
            clientScope.close()
        }
    }
}
