package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.client.renderer.state.level.WorldBorderRenderState
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.Vec3

object WorldBorderRenderEvents : AbstractEventManager<(WorldBorderRenderEvents.WorldBorderRenderContext) -> Unit, WorldBorderRenderEvents.WorldBorderRenderEvent>() {
    override val runTasks: (WorldBorderRenderContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    fun registerWorldBorderRenderEvent(priority: Int = 20, callback: (WorldBorderRenderContext) -> Unit): WorldBorderRenderEvent =
        WorldBorderRenderEvent(priority, callback).register()

    class WorldBorderRenderContext(
        val border: WorldBorder,
        val partialTick: Float,
        val cameraPos: Vec3,
        val renderDistance: Double,
        val state: WorldBorderRenderState
    )

    class WorldBorderRenderEvent(priority: Int, callback: (WorldBorderRenderContext) -> Unit) :
        ManagedTask<(WorldBorderRenderContext) -> Unit, WorldBorderRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
