package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.connection.ConnectionStatusChanged.ConnectionStatusEvent

@EventManager(register = false, name = "registerConnectionStatusChangedEvent")
object ConnectionStatusChanged : AbstractEventManager<(Boolean) -> Unit, ConnectionStatusEvent>() {
    override val runTasks: (Boolean) -> Unit = { connected ->
        safeExecution {
            tasks.forEach { it.callback(connected) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Boolean) -> Unit): ConnectionStatusEvent {
        return ConnectionStatusEvent(priority, callback).register()
    }

    class ConnectionStatusEvent(priority: Int, callback: (Boolean) -> Unit) : ManagedTask<(Boolean) -> Unit, ConnectionStatusEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
