package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.render.ScreenEffectEvents.OverlayContext

@EventManager(register = false)
object FireOverlayEventManager : AbstractEventManager<(OverlayContext) -> Unit, FireOverlayEventManager.FireOverlayEvent>() {
    override val runTasks: (OverlayContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    class FireOverlayEvent(priority: Int, callback: (OverlayContext) -> Unit) :
        ManagedTask<(OverlayContext) -> Unit, FireOverlayEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
