package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.DefaultLong
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

@EventManager
object TickEvents : AbstractEventManager<(Minecraft) -> Unit, TickEvents.TickEvent>(), RegisteredEvent {
    private const val DEFAULT_INTERVAL = 1L

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            runTasks(client)
        }
    }

    override val runTasks: (Minecraft) -> Unit = { client ->
        safeExecution {
            tasks.forEach { task ->
                if (client.level?.gameTime?.rem(task.interval) == 0L) {
                    task.callback(client)
                }
            }
        }
    }

    fun registerTickEvent(priority: Int = DEFAULT_EVENT_PRIORITY, interval: Long = DEFAULT_INTERVAL, callback: (Minecraft) -> Unit): TickEvent {
        return TickEvent(priority, interval, callback).register()
    }

    class TickEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        @DefaultLong(DEFAULT_INTERVAL)
        var interval: Long = DEFAULT_INTERVAL,
        callback: (Minecraft) -> Unit
    ) : ManagedTask<(Minecraft) -> Unit, TickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
