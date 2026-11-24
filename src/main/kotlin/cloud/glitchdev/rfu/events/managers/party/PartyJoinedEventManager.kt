package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object PartyJoinedEventManager : AbstractEventManager<() -> Unit, PartyJoinedEventManager.PartyJoinedEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task ->
                task.callback()
            }
        }
    }

    class PartyJoinedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ) : ManagedTask<() -> Unit, PartyJoinedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
