package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.MinecraftClient

@AutoRegister
object ShutdownEvents : AbstractEventManager<(MinecraftClient) -> Unit, ShutdownEvents.ShutdownEvent>(), RegisteredEvent {
    override fun register() {
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            runTasks(client)
        }
    }

    fun runTasks(client : MinecraftClient) {
        tasks.forEach { task ->
            task.callback(client)
        }
    }

    fun registerShutdownEvent(priority: Int = 20, callback: (MinecraftClient) -> Unit): ShutdownEvent {
        return ShutdownEvent(priority, callback).register()
    }

    class ShutdownEvent(
        priority: Int = 20,
        callback: (MinecraftClient) -> Unit
    ) : ManagedTask<(MinecraftClient) -> Unit, ShutdownEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}