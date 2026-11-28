package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.Camera
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState

@EventManager(register = false)
object CameraFluidEventManager : AbstractEventManager<(CameraFluidEventManager.CameraFluidContext) -> Unit, CameraFluidEventManager.CameraFluidEvent>() {
    override val runTasks: (CameraFluidContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    class CameraFluidContext(val camera: Camera, val state: FluidState, val tag: TagKey<Fluid>, var matches: Boolean) {
        var disableFog: Boolean = false
    }

    class CameraFluidEvent(priority: Int, callback: (CameraFluidContext) -> Unit) :
        ManagedTask<(CameraFluidContext) -> Unit, CameraFluidEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
