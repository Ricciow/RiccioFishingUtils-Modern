package cloud.glitchdev.rfu.events.managers.ui

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object ErrorEvents : AbstractEventManager<(String, String) -> Unit, ErrorEvents.ErrorMessageEvent>() {
    override val runTasks: (String, String) -> Unit = { message, origin ->
        safeExecution {
            tasks.forEach { it.callback(message, origin) }
        }
    }

    class ErrorMessageEvent(priority: Int, callback: (String, String) -> Unit) : ManagedTask<(String, String) -> Unit, ErrorMessageEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
