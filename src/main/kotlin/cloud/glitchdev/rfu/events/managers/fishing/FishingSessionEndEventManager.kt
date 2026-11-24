package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object FishingSessionEndEventManager : AbstractEventManager<() -> Unit, FishingSessionEndEventManager.FishingSessionEndEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task -> task.callback() }
        }
    }

    class FishingSessionEndEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ) : ManagedTask<() -> Unit, FishingSessionEndEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
