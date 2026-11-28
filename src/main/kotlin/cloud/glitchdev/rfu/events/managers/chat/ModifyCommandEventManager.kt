package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object ModifyCommandEventManager : AbstractEventManager<(command: String) -> String, ModifyCommandEventManager.ModifyCommandEvent>() {
    override val runTasks: (command: String) -> String = { command ->
        var result = command
        for (event in tasks) {
            safeExecution(mainThread = false) {
                result = event.callback(result)
            }
        }
        result
    }

    class ModifyCommandEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (command: String) -> String
    ) : ManagedTask<(command: String) -> String, ModifyCommandEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
