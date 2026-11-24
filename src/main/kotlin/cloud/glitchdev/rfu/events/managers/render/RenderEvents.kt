package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents

@EventManager
object RenderEvents : AbstractEventManager<(LevelRenderContext) -> Unit, RenderEvents.RenderEvent>(), RegisteredEvent {
    override fun register() {
        LevelRenderEvents.COLLECT_SUBMITS.register { context ->
            runTasks(context)
        }
    }

    override val runTasks: (LevelRenderContext) -> Unit = { context ->
        safeExecution {
            tasks.forEach { task -> task.callback(context) }
        }
    }

    fun registerRenderEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (LevelRenderContext) -> Unit): RenderEvent {
        return RenderEvent(priority, callback).register()
    }

    class RenderEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (LevelRenderContext) -> Unit
    ) : ManagedTask<(LevelRenderContext) -> Unit, RenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
