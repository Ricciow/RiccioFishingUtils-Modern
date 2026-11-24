package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object ConnectionEvents : RegisteredEvent {

    var wasConnected = false

    override fun register() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            JoinEventManager.runTasks(wasConnected)
            wasConnected = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            DisconnectEventManager.runTasks()
            wasConnected = false
        }
    }

    fun registerJoinEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        delayMillis: Long = JoinEventManager.DEFAULT_JOIN_DELAY_MILLIS,
        callback: (wasConnected: Boolean) -> Unit
    ): JoinEventManager.JoinEvent {
        return JoinEventManager.register(priority, delayMillis, callback)
    }

    fun registerDisconnectEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): DisconnectEventManager.DisconnectEvent {
        return DisconnectEventManager.register(priority, callback)
    }
}
