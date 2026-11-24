package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.managers.fishing.HotSpotChangedEventManager.HotSpotChangedEvent

@EventManager(register = false)
object HotSpotChangedEventManager : AbstractEventManager<(List<Hotspot>) -> Unit, HotSpotChangedEvent>() {
    override val runTasks: (List<Hotspot>) -> Unit = { hotspots ->
        safeExecution {
            tasks.forEach { it.callback(hotspots) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (List<Hotspot>) -> Unit): HotSpotChangedEvent {
        return HotSpotChangedEvent(priority, callback).register()
    }

    class HotSpotChangedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (List<Hotspot>) -> Unit
    ) : ManagedTask<(List<Hotspot>) -> Unit, HotSpotChangedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
