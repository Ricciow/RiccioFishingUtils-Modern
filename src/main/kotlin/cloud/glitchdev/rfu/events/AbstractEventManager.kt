package cloud.glitchdev.rfu.events

import java.util.concurrent.CopyOnWriteArrayList

/**
 * A generic event manager that is agnostic to the callback signature.
 * * @param CB The function signature for the callback (e.g., (A) -> Unit, or (A, B) -> Unit)
 * @param T The specific task type implementation
 */
abstract class AbstractEventManager<CB, T : AbstractEventManager.ManagedTask<CB, T>> : RegisteredEvent {

    protected val tasks = CopyOnWriteArrayList<T>()

    fun submitTask(task: T): T {
        tasks.add(task)
        tasks.sortBy { it.priority }
        return task
    }

    fun removeTask(task: T): T {
        tasks.remove(task)
        return task
    }

    /**
     * The task holds the callback of type CB.
     */
    abstract class ManagedTask<CB, T : ManagedTask<CB, T>>(
        open var priority: Int,
        open val callback: CB
    ) {
        abstract fun register(): T
        abstract fun unregister(): T
    }
}