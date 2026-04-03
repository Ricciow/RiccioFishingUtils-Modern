package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager

object ErrorEvents : AbstractEventManager<(String) -> Unit, ErrorEvents.ErrorMessageEvent>() {
    override val runTasks: (String) -> Unit = { message ->
        safeExecution {
            tasks.forEach { it.callback(message) }
        }
    }

    fun registerErrorMessageEvent(priority: Int = 20, callback: (String) -> Unit): ErrorMessageEvent {
        return ErrorMessageEvent(priority, callback).register()
    }

    class ErrorMessageEvent(priority: Int, callback: (String) -> Unit) : ManagedTask<(String) -> Unit, ErrorMessageEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }

    fun trigger(message: String) {
        runTasks(message)
    }
}
