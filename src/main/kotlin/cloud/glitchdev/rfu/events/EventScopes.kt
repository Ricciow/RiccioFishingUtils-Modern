package cloud.glitchdev.rfu.events

internal class EventScopes {
    private sealed interface OwnerInitializer {
        data class Single(val initialize: (EventScope) -> Unit) : OwnerInitializer
        data class Isolated(val build: IsolatedScopeBuilder.() -> Unit) : OwnerInitializer
    }

    @Volatile
    var activeScope: EventScope? = null
        private set

    fun load(initialize: (EventScope) -> Unit): EventScope? {
        activeScope?.let { return it }
        val shouldMount = synchronized(stateLock) {
            if (clientScope.isClosed) return null
            owners[this] = OwnerInitializer.Single(initialize)
            !stopped
        }
        if (!shouldMount) return null
        return mountSingle(initialize, removeOnFailure = true)
    }

    fun loadIsolated(build: IsolatedScopeBuilder.() -> Unit): EventScope? {
        activeScope?.let { return it }
        val shouldMount = synchronized(stateLock) {
            if (clientScope.isClosed) return null
            owners[this] = OwnerInitializer.Isolated(build)
            !stopped
        }
        if (!shouldMount) return null
        return mountIsolated(build, removeOnFailure = true)
    }

    private fun remount(entry: OwnerInitializer) {
        if (activeScope != null) return
        when (entry) {
            is OwnerInitializer.Single -> mountSingle(entry.initialize, removeOnFailure = false)
            is OwnerInitializer.Isolated -> mountIsolated(entry.build, removeOnFailure = false)
        }
    }

    private fun mountSingle(initialize: (EventScope) -> Unit, removeOnFailure: Boolean): EventScope? {
        val scope = createRestartableChild(removeOnFailure) ?: return null
        try {
            initialize(scope)
        } catch (failure: Throwable) {
            try {
                if (removeOnFailure) {
                    synchronized(stateLock) { owners.remove(this) }
                }
                if (activeScope === scope) activeScope = null
                scope.close()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
        return scope
    }

    private fun mountIsolated(build: IsolatedScopeBuilder.() -> Unit, removeOnFailure: Boolean): EventScope? {
        val scope = createRestartableChild(removeOnFailure) ?: return null
        val builder = IsolatedScopeBuilder(scope)
        builder.build()
        builder.failure?.let { throw it }
        return scope
    }

    private fun createRestartableChild(removeOnFailure: Boolean): EventScope? {
        val scope = try {
            synchronized(stateLock) {
                if (clientScope.isClosed || stopped) return null
                restartableScope.child()
            }
        } catch (failure: Throwable) {
            if (removeOnFailure) {
                synchronized(stateLock) { owners.remove(this) }
            }
            throw failure
        }
        activeScope = scope
        scope.onClose {
            if (activeScope === scope) activeScope = null
        }
        return scope
    }

    fun close() {
        synchronized(stateLock) {
            owners.remove(this)
        }
        val scope = activeScope
        activeScope = null
        scope?.close()
    }

    companion object {
        private val stateLock = Any()
        private val clientScope = EventScope()
        /** Owns startup listeners until the client shuts down. */
        val startup = clientScope.child()
        private var restartableScope = clientScope.child()
        private val owners = linkedMapOf<EventScopes, OwnerInitializer>()
        @Volatile
        private var stopped = false

        val isStopped: Boolean
            get() = stopped

        inline fun withStartupScope(action: (EventScope) -> Unit) {
            if (startup.isClosed) return
            try {
                action(startup)
            } catch (error: IllegalStateException) {
                if (!startup.isClosed) throw error
            }
        }

        fun stop(): Boolean {
            val previousRoot = synchronized(stateLock) {
                if (clientScope.isClosed || stopped) return false
                stopped = true
                restartableScope
            }
            previousRoot.close()
            return true
        }

        fun start(): Boolean {
            val ownersToMount = synchronized(stateLock) {
                if (clientScope.isClosed || !stopped) return false
                stopped = false
                restartableScope = clientScope.child()
                owners.keys.toList()
            }
            runScopeActions(buildMountActions(ownersToMount))
            return true
        }

        fun restart() {
            val previousOwners = synchronized(stateLock) {
                if (clientScope.isClosed) return
                owners.keys.toList()
            }
            val closeRoot: () -> Unit = {
                val previousRoot = synchronized(stateLock) {
                    stopped = false
                    restartableScope
                }
                try {
                    previousRoot.close()
                } finally {
                    synchronized(stateLock) {
                        if (!clientScope.isClosed && !stopped) {
                            restartableScope = clientScope.child()
                        }
                    }
                }
            }
            runScopeActions(listOf(closeRoot) + buildMountActions(previousOwners))
        }

        private fun buildMountActions(ownerList: List<EventScopes>): List<() -> Unit> =
            ownerList.map { owner ->
                {
                    val entry = synchronized(stateLock) {
                        if (clientScope.isClosed || stopped) null else owners[owner]
                    }
                    entry?.let { owner.remount(it) }
                }
            }

        fun shutdown() {
            synchronized(stateLock) {
                stopped = true
                owners.clear()
            }
            clientScope.close()
        }
    }
}
