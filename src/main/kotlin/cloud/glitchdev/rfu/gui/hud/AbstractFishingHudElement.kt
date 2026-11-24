package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerFishingSessionEndEvent
import cloud.glitchdev.rfu.events.managers.registerFishingSessionPauseEvent
import cloud.glitchdev.rfu.events.managers.registerFishingSessionResumeEvent
import cloud.glitchdev.rfu.events.managers.registerFishingSessionStartEvent
import cloud.glitchdev.rfu.events.managers.registerFishingSessionUpdatedEvent
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

    override fun onInitialize(scope: EventScope) {
        super.onInitialize(scope)
        scope.registerFishingSessionStartEvent {
            updateState()
        }
        scope.registerFishingSessionUpdatedEvent {
            updateState()
        }
        scope.registerFishingSessionEndEvent {
            updateState()
        }
        scope.registerFishingSessionPauseEvent {
            updateState()
        }
        scope.registerFishingSessionResumeEvent {
            updateState()
        }
    }
}
