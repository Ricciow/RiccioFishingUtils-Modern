package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.events.AbstractEventManager.ManagedTask

/** Owns listeners and cleanup for a client-thread lifecycle. */
class EventScope : AutoCloseable {
    private val cleanup = mutableListOf<() -> Unit>()
    private var closed = false

    internal fun <T : ManagedTask<*, *>> registerTask(register: () -> T): T =
        register(register) { it.unregister() }

    internal fun <F : ManagedTask<*, *>, S : ManagedTask<*, *>> registerTaskPair(
        register: () -> Pair<F, S>,
    ): Pair<F, S> = register(register) { (first, second) ->
        runCleanup(listOf({ second.unregister() }, { first.unregister() }))
    }

    private fun <T> register(register: () -> T, unregister: (T) -> Unit): T {
        check(!closed) { "Cannot register an event in a closed scope." }
        val result = register()
        if (closed) {
            unregister(result)
            error("The scope closed during event registration.")
        }
        cleanup.add { unregister(result) }
        return result
    }

    fun onClose(action: () -> Unit) {
        check(!closed) { "Cannot add cleanup to a closed scope." }
        cleanup.add(action)
    }

    fun child(): EventScope {
        val child = EventScope()
        val closeChild: () -> Unit = child::close
        onClose(closeChild)
        child.onClose { cleanup.remove(closeChild) }
        return child
    }

    override fun close() {
        if (closed) return
        closed = true
        val actions = cleanup.toList()
        cleanup.clear()
        runCleanup(actions.asReversed())
    }

    private fun runCleanup(actions: List<() -> Unit>) {
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
}
