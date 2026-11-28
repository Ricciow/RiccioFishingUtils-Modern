package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.events.AbstractEventManager.ManagedTask

/** Owns listeners and cleanup for a scoped lifecycle. */
class EventScope : AutoCloseable {
    private val lock = Any()
    private val cleanup = mutableListOf<() -> Unit>()
    private var closed = false

    val isClosed: Boolean
        get() = synchronized(lock) { closed }

    internal fun <T : ManagedTask<*, *>> registerTask(register: () -> T): T =
        register(register) { it.unregister() }

    internal fun <F : ManagedTask<*, *>, S : ManagedTask<*, *>> registerTaskPair(
        register: () -> Pair<F, S>,
    ): Pair<F, S> = register(register) { (first, second) ->
        runScopeActions(listOf({ second.unregister() }, { first.unregister() }))
    }

    private fun <T> register(register: () -> T, unregister: (T) -> Unit): T {
        synchronized(lock) {
            check(!closed) { "Cannot register an event in a closed scope." }
        }
        val result = register()
        val closedDuringRegistration = synchronized(lock) {
            if (closed) {
                true
            } else {
                cleanup.add { unregister(result) }
                false
            }
        }
        if (closedDuringRegistration) {
            unregister(result)
            error("The scope closed during event registration.")
        }
        return result
    }

    fun onClose(action: () -> Unit) {
        synchronized(lock) {
            check(!closed) { "Cannot add cleanup to a closed scope." }
            cleanup.add(action)
        }
    }

    internal fun addCleanupIfOpen(action: () -> Unit): Boolean = synchronized(lock) {
        if (closed) return@synchronized false
        cleanup.add(action)
        true
    }

    internal fun removeCleanup(action: () -> Unit) {
        synchronized(lock) {
            cleanup.remove(action)
        }
    }

    fun child(): EventScope {
        val child = EventScope()
        val closeChild: () -> Unit = child::close
        child.onClose { removeCleanup(closeChild) }
        onClose(closeChild)
        return child
    }

    override fun close() {
        val actions = synchronized(lock) {
            if (closed) return
            closed = true
            val snapshot = cleanup.toList()
            cleanup.clear()
            snapshot
        }
        runScopeActions(actions.asReversed())
    }
}

internal fun runScopeActions(actions: List<() -> Unit>) {
    var failure: Throwable? = null
    actions.forEach { action ->
        try {
            action()
        } catch (error: Throwable) {
            val first = failure
            if (first == null) failure = error
            else if (first !== error) first.addSuppressed(error)
        }
    }
    failure?.let { throw it }
}
