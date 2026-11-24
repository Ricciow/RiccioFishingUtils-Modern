package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.events.AbstractEventManager.ManagedTask

/** Owns listeners and cleanup for a client-thread lifecycle. */
class EventScope : AutoCloseable {
    private val cleanup = mutableListOf<() -> Unit>()
    private var closed = false

    internal fun <T : ManagedTask<*, *>> registerTask(register: () -> T): T {
        check(!closed) { "Cannot register an event in a closed scope." }
        val task = register()
        if (closed) {
            task.unregister()
            error("The scope closed during event registration.")
        }
        cleanup.add { task.unregister() }
        return task
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

        var failure: Throwable? = null
        actions.asReversed().forEach { action ->
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
