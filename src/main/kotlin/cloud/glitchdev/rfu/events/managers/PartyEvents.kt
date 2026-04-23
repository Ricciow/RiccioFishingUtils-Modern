package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket

object PartyEvents {
    object OnPartyChange : AbstractEventManager<(Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit, OnPartyChange.OnPartyChangeEvent>() {
        override val runTasks: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit = { inParty, isLeader, isAllInvite, members ->
            safeExecution {
                tasks.forEach { it.callback(inParty, isLeader, isAllInvite, members) }
            }
        }

        fun register(priority: Int = 20, callback: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit): OnPartyChangeEvent {
            return OnPartyChangeEvent(priority, callback).register()
        }

        class OnPartyChangeEvent(priority: Int, callback: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit) : ManagedTask<(Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit, OnPartyChangeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    fun registerOnPartyChangeEvent(priority: Int = 20, callback: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit) = OnPartyChange.register(priority, callback)
}
