package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.DefaultLong
import cloud.glitchdev.rfu.utils.Coroutines
import kotlinx.coroutines.delay
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object ConnectionEvents : RegisteredEvent {
    private const val DEFAULT_JOIN_DELAY_MILLIS = 0L

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
        delayMillis: Long = DEFAULT_JOIN_DELAY_MILLIS,
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

    @EventManager(register = false)
    object JoinEventManager : AbstractEventManager<(wasConnected : Boolean) -> Unit, JoinEventManager.JoinEvent>() {
        override val runTasks: (Boolean) -> Unit = { wasConnected ->
            tasks.forEach { task ->
                if (task.delayMillis <= 0L) {
                    safeExecution { task.callback(wasConnected) }
                } else {
                    Coroutines.launch {
                        delay(task.delayMillis)
                        safeExecution {
                            if (task in tasks) task.callback(wasConnected)
                        }
                    }
                }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, delayMillis: Long = DEFAULT_JOIN_DELAY_MILLIS, callback: (wasConnected: Boolean) -> Unit): JoinEvent {
            return JoinEvent(priority, delayMillis, callback).register()
        }

        class JoinEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            @DefaultLong(DEFAULT_JOIN_DELAY_MILLIS)
            val delayMillis: Long = DEFAULT_JOIN_DELAY_MILLIS,
            callback: (wasConnected: Boolean) -> Unit,
        ) : ManagedTask<(wasConnected: Boolean) -> Unit, JoinEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object DisconnectEventManager : AbstractEventManager<() -> Unit, DisconnectEventManager.DisconnectEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { it.callback() }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit) : DisconnectEvent {
            return DisconnectEvent(priority, callback).register()
        }

        class DisconnectEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: () -> Unit,
        ) : ManagedTask<() -> Unit, DisconnectEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
