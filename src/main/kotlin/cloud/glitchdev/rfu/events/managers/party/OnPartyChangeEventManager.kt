package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket

@EventManager(register = false)
object OnPartyChangeEventManager : AbstractEventManager<(Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit, OnPartyChangeEventManager.OnPartyChangeEvent>() {
    override val runTasks: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit = { inParty, isLeader, isAllInvite, members ->
        safeExecution {
            tasks.forEach { it.callback(inParty, isLeader, isAllInvite, members) }
        }
    }

    class OnPartyChangeEvent(priority: Int, callback: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit) : ManagedTask<(Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit, OnPartyChangeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
