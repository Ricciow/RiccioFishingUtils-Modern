package cloud.glitchdev.rfu.events

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A generic event manager that is agnostic to the callback signature.
 * * @param CB The function signature for the callback (e.g., (A) -> Unit, or (A, B) -> Unit)
 * @param T The specific task type implementation
 */
abstract class AbstractEventManager<CB, T : AbstractEventManager.ManagedTask<CB, T>> {
    companion object {
        internal const val DEFAULT_EVENT_PRIORITY = 20
    }

    private val tasksLock = Any()
    protected val tasks = CopyOnWriteArrayList<T>()

    fun submitTask(task: T): T {
        synchronized(tasksLock) {
            tasks.add(task)
            tasks.sortBy { it.priority }
        }
        return task
    }

    fun removeTask(task: T): T {
        synchronized(tasksLock) {
            tasks.remove(task)
        }
        return task
    }

    abstract val runTasks : CB

    fun safeExecution(mainThread: Boolean = true, func: () -> Unit) {
        if (mainThread && !mc.isSameThread) {
            mc.execute { executeSafely(func) }
        } else {
            executeSafely(func)
        }
    }

    private fun executeSafely(func: () -> Unit) {
        try {
            func()
        } catch (e: Exception) {
            val managerName = this::class.simpleName ?: "EventManager"
            RFULogger.error("Error in safeExecution ($managerName):", e)
            if (DevSettings.devMode) {
                Chat.sendMessage(
                    TextUtils.debugLiteral(
                        "Error in $managerName: ${e::class.simpleName}: ${e.message ?: "Unknown error"}",
                        TextColor.LIGHT_RED
                    )
                )
            }
        }
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