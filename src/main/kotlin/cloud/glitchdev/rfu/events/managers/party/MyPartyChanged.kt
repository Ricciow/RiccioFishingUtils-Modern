package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.party.FishingParty

@EventManager(register = false)
object MyPartyChanged : AbstractEventManager<(FishingParty?) -> Unit, MyPartyChanged.MyPartyChangedEvent>() {
    override val runTasks: (FishingParty?) -> Unit = { party ->
        safeExecution {
            tasks.forEach { it.callback(party) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingParty?) -> Unit): MyPartyChangedEvent {
        return MyPartyChangedEvent(priority, callback).register()
    }

    class MyPartyChangedEvent(priority: Int, callback: (FishingParty?) -> Unit) : ManagedTask<(FishingParty?) -> Unit, MyPartyChangedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
