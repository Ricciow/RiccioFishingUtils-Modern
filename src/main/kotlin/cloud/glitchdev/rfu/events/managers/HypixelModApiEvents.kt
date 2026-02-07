package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket

@AutoRegister
object HypixelModApiEvents : RegisteredEvent {
    override fun register() {
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket::class.java) { event ->
            LocationEventManager.runTasks(event)
        }

        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket::class.java)
    }

    fun registerLocationEvent(priority: Int = 20, callback: (ClientboundLocationPacket) -> Unit) : LocationEventManager.LocationEvent {
        return LocationEventManager.register(priority, callback)
    }

    object LocationEventManager : AbstractEventManager<(ClientboundLocationPacket) -> Unit, LocationEventManager.LocationEvent>() {
        fun runTasks(packet: ClientboundLocationPacket) {
            for (task in tasks) {
                task.callback(packet)
            }
        }

        fun register(priority: Int = 20, callback: (ClientboundLocationPacket) -> Unit) : LocationEvent {
            return LocationEvent(priority, callback).register()
        }

        class LocationEvent(
            override var priority: Int = 20,
            callback : (ClientboundLocationPacket) -> Unit,
        ) : ManagedTask<(ClientboundLocationPacket) -> Unit, LocationEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}