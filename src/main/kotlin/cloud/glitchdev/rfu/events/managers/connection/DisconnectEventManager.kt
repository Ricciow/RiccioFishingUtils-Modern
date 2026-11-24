package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object DisconnectEventManager : AbstractEventManager<() -> Unit, DisconnectEventManager.DisconnectEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { it.callback() }
        }
    }

    class DisconnectEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit,
    ) : ManagedTask<() -> Unit, DisconnectEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
