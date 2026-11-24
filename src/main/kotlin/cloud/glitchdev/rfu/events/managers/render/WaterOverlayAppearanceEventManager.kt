package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.Identifier
import java.awt.Color

@EventManager(register = false)
object WaterOverlayAppearanceEventManager : AbstractEventManager<(WaterOverlayAppearanceEventManager.WaterOverlayAppearanceContext) -> Unit, WaterOverlayAppearanceEventManager.WaterOverlayAppearanceEvent>() {
    class WaterOverlayAppearanceContext(val player: LocalPlayer) {
        var color: Color? = null
        var texture: Identifier? = null
    }

    override val runTasks: (WaterOverlayAppearanceContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    class WaterOverlayAppearanceEvent(priority: Int, callback: (WaterOverlayAppearanceContext) -> Unit) :
        ManagedTask<(WaterOverlayAppearanceContext) -> Unit, WaterOverlayAppearanceEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
