package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.party.FishingParty

@EventManager(register = false)
object MyPartyChangedEventManager : AbstractEventManager<(FishingParty?) -> Unit, MyPartyChangedEventManager.MyPartyChangedEvent>() {
    override val runTasks: (FishingParty?) -> Unit = { party ->
        safeExecution {
            tasks.forEach { it.callback(party) }
        }
    }

    class MyPartyChangedEvent(priority: Int, callback: (FishingParty?) -> Unit) : ManagedTask<(FishingParty?) -> Unit, MyPartyChangedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
