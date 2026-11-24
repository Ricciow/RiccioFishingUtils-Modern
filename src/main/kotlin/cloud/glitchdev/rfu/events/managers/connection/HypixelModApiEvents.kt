package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket

@AutoRegister
object HypixelModApiEvents : RegisteredEvent {
    val hypixelModAPI : HypixelModAPI = HypixelModAPI.getInstance()

    override fun register(scope: EventScope) {
        hypixelModAPI.createHandler(ClientboundLocationPacket::class.java) { event ->
            LocationEventManager.runTasks(event)
        }

        hypixelModAPI.subscribeToEventPacket(ClientboundLocationPacket::class.java)
    }

}