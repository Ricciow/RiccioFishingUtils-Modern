package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.connection.ConnectionStatusChangedEventManager.ConnectionStatusEvent

@EventManager(register = false, name = "registerConnectionStatusChangedEvent")
object ConnectionStatusChangedEventManager : AbstractEventManager<(Boolean) -> Unit, ConnectionStatusEvent>() {
    override val runTasks: (Boolean) -> Unit = { connected ->
        safeExecution {
            tasks.forEach { it.callback(connected) }
        }
    }

    class ConnectionStatusEvent(priority: Int, callback: (Boolean) -> Unit) : ManagedTask<(Boolean) -> Unit, ConnectionStatusEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
