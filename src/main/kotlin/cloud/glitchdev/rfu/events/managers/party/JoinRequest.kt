package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.utils.dsl.isIgnored

@EventManager(register = false)
object JoinRequest : AbstractEventManager<(String) -> Unit, JoinRequest.JoinRequestEvent>() {
    override val runTasks: (String) -> Unit = { applicant ->
        if (!applicant.isIgnored()) {
            safeExecution {
                tasks.forEach { it.callback(applicant) }
            }
        }
    }

    class JoinRequestEvent(priority: Int, callback: (String) -> Unit) : ManagedTask<(String) -> Unit, JoinRequestEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
