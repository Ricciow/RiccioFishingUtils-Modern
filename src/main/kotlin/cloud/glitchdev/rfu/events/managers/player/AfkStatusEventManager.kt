package cloud.glitchdev.rfu.events.managers.player

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false, name = "registerAfkStatusChangedEvent")
object AfkStatusEventManager : AbstractEventManager<(Boolean) -> Unit, AfkStatusEventManager.AfkStatusEvent>() {
    override val runTasks: (Boolean) -> Unit = { isAfk ->
        safeExecution {
            tasks.forEach { it.callback(isAfk) }
        }
    }

    class AfkStatusEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Boolean) -> Unit
    ) : ManagedTask<(Boolean) -> Unit, AfkStatusEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
