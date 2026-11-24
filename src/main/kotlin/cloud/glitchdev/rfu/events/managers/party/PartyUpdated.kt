package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isIgnored

@EventManager(register = false)
object PartyUpdated : AbstractEventManager<(FishingParty) -> Unit, PartyUpdated.PartyUpdatedEvent>() {
    override val runTasks: (FishingParty) -> Unit = { party ->
        if (!party.user.isIgnored()) {
            safeExecution {
                tasks.forEach { task ->
                    task.callback(party)
                }
            }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingParty) -> Unit): PartyUpdatedEvent {
        return PartyUpdatedEvent(priority, callback).register()
    }

    class PartyUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FishingParty) -> Unit
    ) : ManagedTask<(FishingParty) -> Unit, PartyUpdatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
