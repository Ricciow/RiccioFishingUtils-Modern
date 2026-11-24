package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.render.ScreenEffectEvents.OverlayContext

@EventManager(register = false)
object WaterOverlayEventManager : AbstractEventManager<(OverlayContext) -> Unit, WaterOverlayEventManager.WaterOverlayEvent>() {
    override val runTasks: (OverlayContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (OverlayContext) -> Unit): WaterOverlayEvent =
        WaterOverlayEvent(priority, callback).register()

    class WaterOverlayEvent(priority: Int, callback: (OverlayContext) -> Unit) :
        ManagedTask<(OverlayContext) -> Unit, WaterOverlayEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
