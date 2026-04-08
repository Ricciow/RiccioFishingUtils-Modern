package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.JerryFishing
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.feature.fishing.BlizzardTimer
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HudElement
object BlizzardTimerDisplay : AbstractTextHudElement("blizzardTimer") {

    override val enabled: Boolean
        get() = JerryFishing.blizzardTimerDisplay && (super.enabled || BlizzardTimer.isActive)

    override fun onUpdateState() {
        super.onUpdateState()

        if (!BlizzardTimer.isActive && !isEditing) {
            text.setText("")
            return
        }

        val now = System.currentTimeMillis()
        val remainingMillis = BlizzardTimer.endTimeMillis - now
        val remaining: Duration = if (remainingMillis > 0) remainingMillis.milliseconds else Duration.ZERO

        val timeStr = if (isEditing && !BlizzardTimer.isActive) {
            "10m"
        } else if (BlizzardTimer.isActive && remaining == Duration.ZERO) {
            "???"
        } else {
            remaining.toReadableString()
        }
        text.setText("${AQUAMARINE}${BOLD}Blizzard: ${YELLOW}$timeStr")
    }
}
