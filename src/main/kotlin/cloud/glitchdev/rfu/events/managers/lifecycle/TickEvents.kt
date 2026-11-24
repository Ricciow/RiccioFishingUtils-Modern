package cloud.glitchdev.rfu.events.managers.lifecycle

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.Defaults
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

@EventManager
object TickEvents : AbstractEventManager<(Minecraft) -> Unit, TickEvents.TickEvent>(), RegisteredEvent {
    private const val DEFAULT_INTERVAL = 1L

    override fun register(scope: EventScope) {
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

    @Defaults("interval = ${DEFAULT_INTERVAL}L")
    class TickEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        var interval: Long = DEFAULT_INTERVAL,
        callback: (Minecraft) -> Unit
    ) : ManagedTask<(Minecraft) -> Unit, TickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
