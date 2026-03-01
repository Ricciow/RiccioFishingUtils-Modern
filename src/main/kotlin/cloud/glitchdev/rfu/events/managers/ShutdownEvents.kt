package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft

@AutoRegister
object ShutdownEvents : AbstractEventManager<(Minecraft) -> Unit, ShutdownEvents.ShutdownEvent>(), RegisteredEvent {
    override fun register() {
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            runTasks(client)
        }
    }

    fun runTasks(client : Minecraft) {
        safeExecution {
            tasks.forEach { task ->
                task.callback(client)
            }
        }
    }

    fun registerShutdownEvent(priority: Int = 20, callback: (Minecraft) -> Unit): ShutdownEvent {
        return ShutdownEvent(priority, callback).register()
    }

    class ShutdownEvent(
        priority: Int = 20,
        callback: (Minecraft) -> Unit
    ) : ManagedTask<(Minecraft) -> Unit, ShutdownEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}