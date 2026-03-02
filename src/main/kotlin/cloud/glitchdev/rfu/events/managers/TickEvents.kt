package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

@AutoRegister
object TickEvents : AbstractEventManager<(Minecraft) -> Unit, TickEvents.TickEvent>(), RegisteredEvent {
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

    fun registerTickEvent(priority: Int = 20, interval: Long = 1L, callback: (Minecraft) -> Unit): TickEvent {
        return TickEvent(priority, interval, callback).register()
    }

    class TickEvent(
        priority: Int = 20,
        var interval: Long = 1L,
        callback: (Minecraft) -> Unit
    ) : ManagedTask<(Minecraft) -> Unit, TickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}