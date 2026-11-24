package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket

@AutoRegister
object HypixelModApiEvents : RegisteredEvent {
    val hypixelModAPI : HypixelModAPI = HypixelModAPI.getInstance()

    override fun register() {
        hypixelModAPI.createHandler(ClientboundLocationPacket::class.java) { event ->
            LocationEventManager.runTasks(event)
        }

        hypixelModAPI.subscribeToEventPacket(ClientboundLocationPacket::class.java)
    }

    fun registerLocationEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (ClientboundLocationPacket) -> Unit) : LocationEventManager.LocationEvent {
        return LocationEventManager.register(priority, callback)
    }

    @EventManager(register = false)
    object LocationEventManager : AbstractEventManager<(ClientboundLocationPacket) -> Unit, LocationEventManager.LocationEvent>() {
        override val runTasks: (ClientboundLocationPacket) -> Unit = { packet ->
            safeExecution {
                for (task in tasks) {
                    task.callback(packet)
                }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (ClientboundLocationPacket) -> Unit) : LocationEvent {
            return LocationEvent(priority, callback).register()
        }

        class LocationEvent(
            override var priority: Int = DEFAULT_EVENT_PRIORITY,
            callback : (ClientboundLocationPacket) -> Unit,
        ) : ManagedTask<(ClientboundLocationPacket) -> Unit, LocationEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}