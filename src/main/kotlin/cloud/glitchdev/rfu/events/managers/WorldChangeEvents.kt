package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@AutoRegister
object WorldChangeEvents : AbstractEventManager<() -> Unit, WorldChangeEvents.WorldChangeEvent>(), RegisteredEvent {
    override fun register() {
        ClientPlayConnectionEvents.JOIN.register{ _, _, _ ->
            tasks.forEach { it.callback() }
        }
    }

    fun registerWorldChangeEvent(priority: Int = 20, callback: () -> Unit): WorldChangeEvent {
        return WorldChangeEvent(priority, callback).register()
    }

    class WorldChangeEvent(
        priority: Int = 20,
        callback: () -> Unit,
    ) : ManagedTask<() -> Unit, WorldChangeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}