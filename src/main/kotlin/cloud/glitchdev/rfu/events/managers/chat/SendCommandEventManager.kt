package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object SendCommandEventManager : AbstractEventManager<(command: String) -> Boolean, SendCommandEventManager.SendCommandEvent>() {
    override val runTasks: (command: String) -> Boolean = { command ->
        var result = true
        for (event in tasks) {
            safeExecution {
                if (!event.callback(command)) result = false
            }
        }
        result
    }

    class SendCommandEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (command: String) -> Boolean
    ) : ManagedTask<(command: String) -> Boolean, SendCommandEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
