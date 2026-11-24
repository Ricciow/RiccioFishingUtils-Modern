package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.managers.connection.ConnectionStatusChanged.ConnectionStatusEvent

object WebSocketEvents {
    fun registerConnectionStatusChangedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Boolean) -> Unit): ConnectionStatusEvent {
        return ConnectionStatusChanged.register(priority, callback)
    }

    fun trigger(connected: Boolean) {
        ConnectionStatusChanged.runTasks(connected)
    }
}
