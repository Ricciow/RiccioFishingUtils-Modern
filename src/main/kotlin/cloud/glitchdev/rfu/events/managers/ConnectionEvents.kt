package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.Coroutines
import kotlinx.coroutines.delay
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
        priority: Int = 20,
        delayMillis: Long = 0L,
        callback: (wasConnected: Boolean) -> Unit
    ): JoinEventManager.JoinEvent {
        return JoinEventManager.register(priority, delayMillis, callback)
    }

    fun registerDisconnectEvent(
        priority: Int = 20,
        callback: () -> Unit
    ): DisconnectEventManager.DisconnectEvent {
        return DisconnectEventManager.register(priority, callback)
    }

    object JoinEventManager : AbstractEventManager<(wasConnected : Boolean) -> Unit, JoinEventManager.JoinEvent>() {
        override val runTasks: (Boolean) -> Unit = { wasConnected ->
            tasks.forEach { task ->
                if (task.delayMillis <= 0L) {
                    safeExecution { task.callback(wasConnected) }
                } else {
                    Coroutines.launch {
                        delay(task.delayMillis)
                        safeExecution { task.callback(wasConnected) }
                    }
                }
            }
        }

        fun register(priority: Int = 20, delayMillis: Long = 0L, callback: (wasConnected: Boolean) -> Unit): JoinEvent {
            return JoinEvent(priority, delayMillis, callback).register()
        }

        class JoinEvent(
            priority: Int = 20,
            val delayMillis: Long = 0L,
            callback: (wasConnected: Boolean) -> Unit,
        ) : ManagedTask<(wasConnected: Boolean) -> Unit, JoinEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object DisconnectEventManager : AbstractEventManager<() -> Unit, DisconnectEventManager.DisconnectEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { it.callback() }
            }
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
