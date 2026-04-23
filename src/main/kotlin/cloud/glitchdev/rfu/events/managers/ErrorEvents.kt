package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager

object ErrorEvents : AbstractEventManager<(String, String) -> Unit, ErrorEvents.ErrorMessageEvent>() {
    override val runTasks: (String, String) -> Unit = { message, origin ->
        safeExecution {
            tasks.forEach { it.callback(message, origin) }
        }
    }

    fun registerErrorMessageEvent(priority: Int = 20, callback: (String, String) -> Unit): ErrorMessageEvent {
        return ErrorMessageEvent(priority, callback).register()
    }

    class ErrorMessageEvent(priority: Int, callback: (String, String) -> Unit) : ManagedTask<(String, String) -> Unit, ErrorMessageEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }

    fun trigger(message: String, origin: String) {
        runTasks(message, origin)
    }
}
