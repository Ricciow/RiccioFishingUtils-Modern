package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager

object CloseConfigEvents : AbstractEventManager<() -> Unit, CloseConfigEvents.CloseConfigEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task ->
                task.callback()
            }
        }
    }

    fun registerCloseConfigEvent(priority: Int = 20, callback: () -> Unit): CloseConfigEvent {
        return CloseConfigEvent(priority, callback).register()
    }

    class CloseConfigEvent(
        priority: Int = 20,
        callback: () -> Unit
    ) : ManagedTask<() -> Unit, CloseConfigEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}