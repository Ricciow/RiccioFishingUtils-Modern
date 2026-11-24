package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.render.ScreenEffectEvents.WaterOverlayAppearanceContext

@EventManager(register = false)
object WaterOverlayAppearanceEventManager : AbstractEventManager<(WaterOverlayAppearanceContext) -> Unit, WaterOverlayAppearanceEventManager.WaterOverlayAppearanceEvent>() {
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
