package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.fishing.BobbinTimeBonus
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.feature.fishing.BobbinTime
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import kotlin.math.roundToInt

@HudElement
object BobbinTimeDisplay : AbstractFishingHudElement("bobbinTime") {
    override val relatedSetting = "Bobbin' Time"
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
        val rounded = (percentageVal * 10).roundToInt() / 10.0
        val bonusValue = if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }

        val selectedBonuses = GeneralFishing.bobbinTimeBonuses
        val bonuses = BobbinTimeBonus.entries.filter { it in selectedBonuses }
        val countText = "${LIGHT_GREEN}${BOLD}Bobbin: ${YELLOW}$displayCount"
        val bonusText = bonuses.joinToString("${GRAY}, ") { "${it.color}$bonusValue${it.suffix}" }
        text.setText(if (bonuses.isEmpty()) countText else "$countText $GRAY($bonusText$GRAY)")
    }
}
