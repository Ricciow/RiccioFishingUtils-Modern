package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object ConnectionEvents : RegisteredEvent {
    var wasConnected = false

    override fun register() {
        ClientPlayConnectionEvents.JOIN.register{ _, _, _ ->
            JoinEventManager.runTasks(wasConnected)
            wasConnected = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            println("Disconnected")
            DisconnectEventManager.runTasks()
            wasConnected = false
        }
    }

    fun registerJoinEvent(priority: Int = 20, callback: (wasConnected: Boolean) -> Unit): JoinEventManager.JoinEvent {
        return JoinEventManager.register(priority, callback)
    }

    object JoinEventManager : AbstractEventManager<(wasConnected : Boolean) -> Unit, JoinEventManager.JoinEvent>() {
        fun runTasks(wasConnected: Boolean) {
            tasks.forEach { it.callback(wasConnected) }
        }

        fun register(priority: Int = 20, callback: (wasConnected : Boolean) -> Unit) : JoinEvent {
            return JoinEvent(priority, callback).register()
        }

        class JoinEvent(
            priority: Int = 20,
            callback: (wasConnected : Boolean) -> Unit,
        ) : ManagedTask<(wasConnected : Boolean) -> Unit, JoinEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object DisconnectEventManager : AbstractEventManager<() -> Unit, DisconnectEventManager.DisconnectEvent>() {
        fun runTasks() {
            tasks.forEach { it.callback() }
        }

        fun register(priority: Int = 20, callback: () -> Unit) : DisconnectEvent {
            return DisconnectEvent(priority, callback).register()
        }

        class DisconnectEvent(
            priority: Int = 20,
            callback: () -> Unit,
        ) : ManagedTask<() -> Unit, DisconnectEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}