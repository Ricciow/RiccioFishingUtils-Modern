package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.party.FishingParty

@EventManager(register = false)
object PartyListChanged : AbstractEventManager<(List<FishingParty>) -> Unit, PartyListChanged.PartyListChangedEvent>() {
    override val runTasks: (List<FishingParty>) -> Unit = { parties ->
        safeExecution {
            tasks.forEach { it.callback(parties) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (List<FishingParty>) -> Unit): PartyListChangedEvent {
        return PartyListChangedEvent(priority, callback).register()
    }

    class PartyListChangedEvent(priority: Int, callback: (List<FishingParty>) -> Unit) : ManagedTask<(List<FishingParty>) -> Unit, PartyListChangedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
