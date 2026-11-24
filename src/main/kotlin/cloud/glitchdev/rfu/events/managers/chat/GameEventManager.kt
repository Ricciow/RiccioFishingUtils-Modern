package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.network.chat.Component

@EventManager(register = false, generateRegistration = false)
object GameEventManager : AbstractEventManager<(text: Component, overlay: Boolean) -> Boolean, GameEventManager.GameEvent>() {
    override val runTasks: (text: Component, overlay: Boolean) -> Boolean = { text, overlay ->
        var result = true
        for (event in tasks) {
            safeExecution {
                if (!event.callback(text, overlay)) result = false
            }
        }
        result
    }

    class GameEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component, overlay: Boolean) -> Boolean
    ) : ManagedTask<(text: Component, overlay: Boolean) -> Boolean, GameEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
