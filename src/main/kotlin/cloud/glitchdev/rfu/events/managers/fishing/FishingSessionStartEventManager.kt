package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object FishingSessionStartEventManager : AbstractEventManager<() -> Unit, FishingSessionStartEventManager.FishingSessionStartEvent>() {
    override val runTasks: () -> Unit = {
        safeExecution {
            tasks.forEach { task -> task.callback() }
        }
    }

    class FishingSessionStartEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ) : ManagedTask<() -> Unit, FishingSessionStartEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
