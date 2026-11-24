package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object CloseConfigEvents : AbstractEventManager<() -> Unit, CloseConfigEvents.CloseConfigEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task ->
                task.callback()
            }
        }
    }

    fun registerCloseConfigEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): CloseConfigEvent {
        return CloseConfigEvent(priority, callback).register()
    }

    class CloseConfigEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ) : ManagedTask<() -> Unit, CloseConfigEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}