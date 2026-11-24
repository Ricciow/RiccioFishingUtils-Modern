package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.network.chat.Component

@EventManager(register = false, generateRegistration = false)
object ChatEventManager : AbstractEventManager<(text: Component) -> Boolean, ChatEventManager.ChatEvent>() {
    override val runTasks: (text: Component) -> Boolean = { text ->
        var result = true
        for (event in tasks) {
            safeExecution {
                if (!event.callback(text)) result = false
            }
        }
        result
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (text: Component) -> Boolean): ChatEvent {
        return ChatEvent(priority, callback).register()
    }

    class ChatEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component) -> Boolean
    ) : ManagedTask<(text: Component) -> Boolean, ChatEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
