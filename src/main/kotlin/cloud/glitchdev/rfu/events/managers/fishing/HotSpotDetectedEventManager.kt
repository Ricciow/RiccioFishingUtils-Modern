package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object HotSpotDetectedEventManager : AbstractEventManager<(Hotspot) -> Unit, HotSpotDetectedEventManager.HotSpotDetectedEvent>() {
    override val runTasks: (Hotspot) -> Unit = { hotspot ->
        safeExecution {
            tasks.forEach { it.callback(hotspot) }
        }
    }

    class HotSpotDetectedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Hotspot) -> Unit
    ) : ManagedTask<(Hotspot) -> Unit, HotSpotDetectedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
