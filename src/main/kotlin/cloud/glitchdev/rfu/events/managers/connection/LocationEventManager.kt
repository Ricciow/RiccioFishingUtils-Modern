package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket

@EventManager(register = false)
object LocationEventManager : AbstractEventManager<(ClientboundLocationPacket) -> Unit, LocationEventManager.LocationEvent>() {
    override val runTasks: (ClientboundLocationPacket) -> Unit = { packet ->
        safeExecution {
            for (task in tasks) {
                task.callback(packet)
            }
        }
    }

    class LocationEvent(
        override var priority: Int = DEFAULT_EVENT_PRIORITY,
        callback : (ClientboundLocationPacket) -> Unit,
    ) : ManagedTask<(ClientboundLocationPacket) -> Unit, LocationEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
