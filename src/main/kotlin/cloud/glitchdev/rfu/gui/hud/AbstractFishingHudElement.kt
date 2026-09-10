package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionEndEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionPauseEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionResumeEvent
import cloud.glitchdev.rfu.events.managers.FishingSessionEvents.registerFishingSessionStartEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession

abstract class AbstractFishingHudElement(id: String) : AbstractTextHudElement(id) {
    open val requiresFishing: Boolean = true
    open val displaysWhilePaused: Boolean = false

    override val enabled: Boolean
        get() = forcePreview || (requirement && (isEditing || (isElementActive && (!requiresFishing || FishingSession.isFishing) && (displaysWhilePaused || !FishingSession.isPaused))))

    override fun onInitialize() {
        super.onInitialize()
        registerFishingSessionStartEvent {
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
