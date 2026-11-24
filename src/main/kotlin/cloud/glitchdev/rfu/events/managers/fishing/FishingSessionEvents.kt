package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.feature.fishing.FishingSession.FishingType

object FishingSessionEvents {

    fun registerFishingSessionUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (fishingType: FishingType) -> Unit
    ): FishingSessionUpdatedEventManager.FishingSessionUpdatedEvent {
        return FishingSessionUpdatedEventManager.register(priority, callback)
    }

    fun registerFishingSessionStartEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionStartEventManager.FishingSessionStartEvent {
        return FishingSessionStartEventManager.register(priority, callback)
    }

    fun registerFishingSessionEndEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionEndEventManager.FishingSessionEndEvent {
        return FishingSessionEndEventManager.register(priority, callback)
    }

    fun registerFishingSessionPauseEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionPauseEventManager.FishingSessionPauseEvent {
        return FishingSessionPauseEventManager.register(priority, callback)
    }

    fun registerFishingSessionResumeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: () -> Unit
    ): FishingSessionResumeEventManager.FishingSessionResumeEvent {
        return FishingSessionResumeEventManager.register(priority, callback)
    }
}
