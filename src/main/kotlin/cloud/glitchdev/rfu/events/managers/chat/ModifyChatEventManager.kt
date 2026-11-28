package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object ModifyChatEventManager : AbstractEventManager<(message: String) -> String, ModifyChatEventManager.ModifyChatEvent>() {
    override val runTasks: (message: String) -> String = { message ->
        var result = message
        for (event in tasks) {
            safeExecution(mainThread = false) {
                result = event.callback(result)
            }
        }
        result
    }

    class ModifyChatEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (message: String) -> String
    ) : ManagedTask<(message: String) -> String, ModifyChatEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
