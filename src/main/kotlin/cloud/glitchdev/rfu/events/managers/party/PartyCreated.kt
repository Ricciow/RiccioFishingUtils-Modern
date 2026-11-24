package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isIgnored

@EventManager(register = false)
object PartyCreated : AbstractEventManager<(FishingParty) -> Unit, PartyCreated.PartyCreatedEvent>() {
    override val runTasks: (FishingParty) -> Unit = { party ->
        if (!party.user.isIgnored()) {
            safeExecution {
                tasks.forEach { task ->
                    task.callback(party)
                }
            }
        }
    }

    class PartyCreatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FishingParty) -> Unit
    ) : ManagedTask<(FishingParty) -> Unit, PartyCreatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
