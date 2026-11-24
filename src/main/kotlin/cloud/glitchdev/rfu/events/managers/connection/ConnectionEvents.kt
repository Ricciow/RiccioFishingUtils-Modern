package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object ConnectionEvents : RegisteredEvent {

    var wasConnected = false

    override fun register(scope: EventScope) {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            JoinEventManager.runTasks(wasConnected)
            wasConnected = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            DisconnectEventManager.runTasks()
            wasConnected = false
        }
    }

}
