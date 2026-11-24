package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object SendChatEventManager : AbstractEventManager<(message: String) -> Boolean, SendChatEventManager.SendChatEvent>() {
    override val runTasks: (message: String) -> Boolean = { message ->
        var result = true
        for (event in tasks) {
            safeExecution {
                if (!event.callback(message)) result = false
            }
        }
        result
    }

    class SendChatEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (message: String) -> Boolean
    ) : ManagedTask<(message: String) -> Boolean, SendChatEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
