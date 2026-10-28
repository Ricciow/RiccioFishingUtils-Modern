package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.renderer.block.FluidStateModelSet
import net.minecraft.world.level.material.FluidState
import java.awt.Color

object FluidRenderEvents : AbstractEventManager<(FluidRenderEvents.FluidRenderContext) -> Unit, FluidRenderEvents.FluidRenderEvent>() {
    override val runTasks: (FluidRenderContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    fun registerFluidRenderEvent(priority: Int = 20, callback: (FluidRenderContext) -> Unit): FluidRenderEvent =
        FluidRenderEvent(priority, callback).register()

    class FluidRenderContext(val fluidState: FluidState, val models: FluidStateModelSet, var model: FluidModel) {
        var color: Color? = null
    }

    class FluidRenderEvent(priority: Int, callback: (FluidRenderContext) -> Unit) :
        ManagedTask<(FluidRenderContext) -> Unit, FluidRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}