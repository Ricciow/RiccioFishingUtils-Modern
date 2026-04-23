package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.managers.WebSocketEvents.ConnectionStatusChanged.ConnectionStatusEvent

object WebSocketEvents {
    fun registerConnectionStatusChangedEvent(priority: Int = 20, callback: (Boolean) -> Unit): ConnectionStatusEvent {
        return ConnectionStatusChanged.register(priority, callback)
    }

    object ConnectionStatusChanged : AbstractEventManager<(Boolean) -> Unit, ConnectionStatusEvent>() {
        override val runTasks: (Boolean) -> Unit = { connected ->
            safeExecution {
                tasks.forEach { it.callback(connected) }
            }
        }

        fun register(priority: Int = 20, callback: (Boolean) -> Unit): ConnectionStatusEvent {
            return ConnectionStatusEvent(priority, callback).register()
        }

        class ConnectionStatusEvent(priority: Int, callback: (Boolean) -> Unit) : ManagedTask<(Boolean) -> Unit, ConnectionStatusEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    fun trigger(connected: Boolean) {
        ConnectionStatusChanged.runTasks(connected)
    }
}
