package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
//~if >=26.1 'world.World' -> 'level.Level' {
//~if >=26.1 'World' -> 'Level' {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents

@AutoRegister
object RenderEvents : AbstractEventManager<(LevelRenderContext) -> Unit, RenderEvents.RenderEvent>(), RegisteredEvent {
    override fun register() {
        //~if >=26.1 'AFTER_ENTITIES' -> 'AFTER_TRANSLUCENT_FEATURES' {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register { context ->
            runTasks(context)
        }
        //~}
    }

    override val runTasks: (LevelRenderContext) -> Unit = { context ->
        safeExecution {
            tasks.forEach { task -> task.callback(context) }
        }
    }

    fun registerRenderEvent(priority: Int = 20, callback: (LevelRenderContext) -> Unit): RenderEvent {
        return RenderEvent(priority, callback).register()
    }

    class RenderEvent(
        priority: Int = 20,
        callback: (LevelRenderContext) -> Unit
    ) : ManagedTask<(LevelRenderContext) -> Unit, RenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
//~}
//~}
