package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents

@AutoRegister
object RenderEvents : AbstractEventManager<(WorldRenderContext) -> Unit, RenderEvents.RenderEvent>(), RegisteredEvent {

    override fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            runTasks(context)
        }
    }

    override val runTasks: (WorldRenderContext) -> Unit = { context ->
        safeExecution {
            tasks.forEach { task -> task.callback(context) }
        }
    }

    fun registerRenderEvent(priority: Int = 20, callback: (WorldRenderContext) -> Unit): RenderEvent {
        return RenderEvent(priority, callback).register()
    }

    class RenderEvent(
        priority: Int = 20,
        callback: (WorldRenderContext) -> Unit
    ) : ManagedTask<(WorldRenderContext) -> Unit, RenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}