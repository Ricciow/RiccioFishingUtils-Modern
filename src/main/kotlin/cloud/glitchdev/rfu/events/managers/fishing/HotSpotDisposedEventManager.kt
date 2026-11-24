package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false, name = "registerHotSpotDisposeEvent")
object HotSpotDisposedEventManager : AbstractEventManager<(Hotspot) -> Unit, HotSpotDisposedEventManager.HotSpotDisposedEvent>() {
    override val runTasks: (Hotspot) -> Unit = { hotspot ->
        safeExecution {
            tasks.forEach { it.callback(hotspot) }
        }
    }

    class HotSpotDisposedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Hotspot) -> Unit
    ) : ManagedTask<(Hotspot) -> Unit, HotSpotDisposedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
