package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient

@AutoRegister
object TickEvents : AbstractEventManager<(MinecraftClient) -> Unit, TickEvents.TickEvent>(), RegisteredEvent {
    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            tasks.forEach { task ->
                if (client.world?.time?.rem(task.interval) == 0L) {
                    task.callback(client)
                }
            }
        }
    }

    fun registerTickEvent(priority: Int = 20, interval: Long = 1L, callback: (MinecraftClient) -> Unit): TickEvent {
        return TickEvent(priority, interval, callback).register()
    }

    class TickEvent(
        priority: Int = 20,
        var interval: Long = 1L,
        callback: (MinecraftClient) -> Unit
    ) : ManagedTask<(MinecraftClient) -> Unit, TickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}