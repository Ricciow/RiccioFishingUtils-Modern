package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.feature.fishing.FishingSession

abstract class AbstractFishingHudElement(id: String) : AbstractTextHudElement(id) {
    open val requiresFishing: Boolean = true
    open val displaysWhilePaused: Boolean = false

    override val enabled: Boolean
        get() = requirement && (isEditing || (isElementActive && (!requiresFishing || FishingSession.isFishing) && (displaysWhilePaused || !FishingSession.isPaused)))
}
