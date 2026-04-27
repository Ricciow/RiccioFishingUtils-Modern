package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.GOLD
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.feature.other.BestiaryDisplay
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement

@HudElement
object BestiaryHUD : AbstractTextHudElement("bestiaryHUD") {
    override val enabled: Boolean
        get() = OtherSettings.bestiaryDisplay && (super.enabled || BestiaryDisplay.bestiaries.isNotEmpty())

    override fun onUpdateState() {
        super.onUpdateState()

        val lines = mutableListOf<String>()
        
        if (BestiaryDisplay.bestiaries.isEmpty()) {
            if (isEditing) {
                text.setText("${CYAN}${BOLD}Bestiary HUD")
            } else {
                text.setText("")
            }
            return
        }

        BestiaryDisplay.bestiaries.forEach { entry ->
            lines.add("$CYAN$BOLD${entry.name} $AQUAMARINE${entry.currentTier+1}$WHITE: $GOLD${entry.current}$CYAN/$YELLOW${entry.goal}")
        }

        text.setText(lines.joinToString("\n"))
    }
}
