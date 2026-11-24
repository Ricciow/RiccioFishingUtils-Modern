package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.feature.fishing.FishingSession.FishingType

@EventManager(register = false)
object FishingSessionUpdatedEventManager : AbstractEventManager<(FishingType) -> Unit, FishingSessionUpdatedEventManager.FishingSessionUpdatedEvent>() {
    override val runTasks: (FishingType) -> Unit = { fishingType ->
        safeExecution {
            tasks.forEach { task -> task.callback(fishingType) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingType) -> Unit): FishingSessionUpdatedEvent {
        return FishingSessionUpdatedEvent(priority, callback).register()
    }

    class FishingSessionUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FishingType) -> Unit
    ) : ManagedTask<(FishingType) -> Unit, FishingSessionUpdatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
