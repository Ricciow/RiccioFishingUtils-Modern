package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object InfoMessageEventManager : AbstractEventManager<(String, Boolean) -> Unit, InfoMessageEventManager.InfoMessageEvent>() {
    override val runTasks: (String, Boolean) -> Unit = { message, isWarning ->
        safeExecution {
            tasks.forEach { it.callback(message, isWarning) }
        }
    }

    class InfoMessageEvent(priority: Int, callback: (String, Boolean) -> Unit) : ManagedTask<(String, Boolean) -> Unit, InfoMessageEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
