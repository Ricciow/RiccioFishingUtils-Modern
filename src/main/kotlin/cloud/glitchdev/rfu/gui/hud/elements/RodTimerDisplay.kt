package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.width

@HudElement
object RodTimerDisplay : AbstractTextHudElement("rodTimer") {
    var rodTime : Float = -1f
    override val requirement: Boolean
        get() = GeneralFishing.rodTimerDisplay
    override val isElementActive: Boolean
        get() = rodTime >= 0

    override fun onUpdateState() {
        super.onUpdateState()

        val readyText = "$LIGHT_RED$BOLD${GeneralFishing.rodTimerReadyText.toMcCodes()}"

        container.constrain {
            width = if(rodTime == 0f) {
                readyText.width(scale).pixels()
            } else {
                ChildBasedSizeConstraint()
            }
        }

        val string = if(rodTime > 0) {
            "$YELLOW$BOLD$rodTime"
        } else if(rodTime == 0f) {
            readyText
        } else {
            "$YELLOW${BOLD}3.0"
        }

        text.setText(string)
    }
}
