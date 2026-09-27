package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionEndEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionPauseEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionResumeEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionStartEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionUpdatedEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession

abstract class AbstractFishingHudElement(id: String) : AbstractTextHudElement(id) {
    open val requiresFishing: Boolean = true
    open val requiresTrophyFishing: Boolean = false
    open val requiresTreasureFishing: Boolean = false
    open val hideWhileTrophyFishing: Boolean = false
    open val hideWhileTreasureFishing: Boolean = false
    open val displaysWhilePaused: Boolean = false

    override val enabled: Boolean
        get() = forcePreview || (
            requirement && (
                isEditing || (
                    isElementActive &&
                    (!requiresFishing || FishingSession.isFishing) &&
                    (!requiresTrophyFishing || FishingSession.isTrophyFishing) &&
                    (!requiresTreasureFishing || FishingSession.isTreasureFishing) &&
                    (!hideWhileTrophyFishing || !FishingSession.isTrophyFishing) &&
                    (!hideWhileTreasureFishing || !FishingSession.isTreasureFishing) &&
                    (displaysWhilePaused || !FishingSession.isPaused)
                )
            )
        )

    override fun onInitialize() {
        super.onInitialize()
        registerFishingSessionStartEvent {
            updateState()
        }
        registerFishingSessionUpdatedEvent {
            updateState()
        }
        registerFishingSessionEndEvent {
            updateState()
        }
        registerFishingSessionPauseEvent {
            updateState()
        }
        registerFishingSessionResumeEvent {
            updateState()
        }
    }
}
