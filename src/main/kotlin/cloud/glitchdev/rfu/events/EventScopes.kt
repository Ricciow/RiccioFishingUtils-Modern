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

internal class EventScopes {
    private sealed interface OwnerInitializer {
        data class Single(val initialize: (EventScope) -> Unit) : OwnerInitializer
        data class Isolated(val build: IsolatedScopeBuilder.() -> Unit) : OwnerInitializer
    }

    @Volatile
    var activeScope: EventScope? = null
        private set

    fun load(initialize: (EventScope) -> Unit): EventScope {
        activeScope?.let { return it }
        synchronized(stateLock) {
            owners[this] = OwnerInitializer.Single(initialize)
        }
        return mountSingle(initialize, removeOnFailure = true)
    }

    fun loadIsolated(build: IsolatedScopeBuilder.() -> Unit): EventScope {
        activeScope?.let { return it }
        synchronized(stateLock) {
            owners[this] = OwnerInitializer.Isolated(build)
        }
        return mountIsolated(build, removeOnFailure = true)
    }

    private fun remount(entry: OwnerInitializer) {
        if (activeScope != null) return
        when (entry) {
            is OwnerInitializer.Single -> mountSingle(entry.initialize, removeOnFailure = false)
            is OwnerInitializer.Isolated -> mountIsolated(entry.build, removeOnFailure = false)
        }
    }

    private fun mountSingle(initialize: (EventScope) -> Unit, removeOnFailure: Boolean): EventScope {
        val scope = createRestartableChild(removeOnFailure)
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

    private fun mountIsolated(build: IsolatedScopeBuilder.() -> Unit, removeOnFailure: Boolean): EventScope {
        val scope = createRestartableChild(removeOnFailure)
        val builder = IsolatedScopeBuilder(scope)
        builder.build()
        builder.failure?.let { throw it }
        return scope
    }

    private fun createRestartableChild(removeOnFailure: Boolean): EventScope {
        val scope = try {
            synchronized(stateLock) { restartableScope.child() }
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

        inline fun withStartupScope(action: (EventScope) -> Unit) {
            if (startup.isClosed) return
            try {
                action(startup)
            } catch (error: IllegalStateException) {
                if (!startup.isClosed) throw error
            }
        }

        fun restart() {
            val previousOwners = synchronized(stateLock) {
                if (clientScope.isClosed) return
                owners.keys.toList()
            }
            val closeRoot: () -> Unit = {
                val previousRoot = synchronized(stateLock) { restartableScope }
                try {
                    previousRoot.close()
                } finally {
                    synchronized(stateLock) {
                        if (!clientScope.isClosed) {
                            restartableScope = clientScope.child()
                        }
                    }
                }
            }
            val restoreOwners: List<() -> Unit> = previousOwners.map { owner ->
                {
                    val entry = synchronized(stateLock) {
                        if (clientScope.isClosed) null else owners[owner]
                    }
                    entry?.let { owner.remount(it) }
                }
            }
            runScopeActions(listOf(closeRoot) + restoreOwners)
        }

        fun shutdown() {
            synchronized(stateLock) {
                owners.clear()
            }
            clientScope.close()
        }
    }
}
