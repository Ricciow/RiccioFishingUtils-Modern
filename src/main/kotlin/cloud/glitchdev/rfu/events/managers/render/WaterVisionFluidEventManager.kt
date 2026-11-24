package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.player.LocalPlayer

@EventManager(register = false)
object WaterVisionFluidEventManager : AbstractEventManager<(WaterVisionFluidEventManager.WaterVisionFluidContext) -> Unit, WaterVisionFluidEventManager.WaterVisionFluidEvent>() {
    override val runTasks: (WaterVisionFluidContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    class WaterVisionFluidContext(val player: LocalPlayer, var submerged: Boolean)

    class WaterVisionFluidEvent(priority: Int, callback: (WaterVisionFluidContext) -> Unit) :
        ManagedTask<(WaterVisionFluidContext) -> Unit, WaterVisionFluidEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
