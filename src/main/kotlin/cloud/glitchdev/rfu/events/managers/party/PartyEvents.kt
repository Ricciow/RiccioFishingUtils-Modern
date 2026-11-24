package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket

object PartyEvents {

    fun registerOnPartyChangeEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Boolean, Boolean, Boolean, Map<String, ClientboundPartyInfoPacket.PartyRole>) -> Unit) = OnPartyChange.register(priority, callback)
}
