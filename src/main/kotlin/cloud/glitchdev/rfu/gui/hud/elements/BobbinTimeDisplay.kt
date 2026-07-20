package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_BLUE
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.feature.fishing.BobbinTime

@HudElement
object BobbinTimeDisplay : AbstractFishingHudElement("bobbinTime") {
    override val requirement: Boolean
        get() = GeneralFishing.bobbinTimeDisplay
    override val isElementActive: Boolean
        get() = BobbinTime.hasBobbinTimeArmor

    override fun onUpdateState() {
        super.onUpdateState()

        val displayCount: Int
        val displayPercentage: Double

        if (isEditing && !BobbinTime.hasBobbinTimeArmor && BobbinTime.bobberCount == 0) {
            displayCount = 5
            displayPercentage = 0.20
        } else {
            displayCount = BobbinTime.bobberCount
            displayPercentage = BobbinTime.buffPercentage
        }

        val percentageVal = displayPercentage * 100
        val rounded = Math.round(percentageVal * 10) / 10.0
        val percentageStr = if (rounded % 1.0 == 0.0) {
            "${rounded.toInt()}%"
        } else {
            "$rounded%"
        }

        text.setText("${LIGHT_GREEN}${BOLD}Bobbin: ${YELLOW}$displayCount ${GRAY}(${LIGHT_BLUE}$percentageStr${GRAY})")
    }
}
