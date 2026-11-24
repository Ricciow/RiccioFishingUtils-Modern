package cloud.glitchdev.rfu.events.managers.lifecycle

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft

@EventManager
object ShutdownEvents : AbstractEventManager<(Minecraft) -> Unit, ShutdownEvents.ShutdownEvent>(), RegisteredEvent {
    override fun register(scope: EventScope) {
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            runTasks(client)
        }
    }

    override val runTasks: (Minecraft) -> Unit = { client ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(client)
            }
        }
    }

    class ShutdownEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Minecraft) -> Unit
    ) : ManagedTask<(Minecraft) -> Unit, ShutdownEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}