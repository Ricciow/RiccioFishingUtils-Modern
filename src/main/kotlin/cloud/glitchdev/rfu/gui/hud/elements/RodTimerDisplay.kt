package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
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

    override val enabled: Boolean
        get() = GeneralFishing.rodTimerDisplay && (super.enabled || rodTime >= 0)

    val container = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    init {
        this.removeChild(text)
        text childOf container
    }

    override fun onUpdateState() {
        super.onUpdateState()

        container.constrain {
            width = if(rodTime == 0f) {
                "3.0".width(scale).pixels()
            } else {
                ChildBasedSizeConstraint()
            }
        }

        val string = if(rodTime > 0) {
            "$YELLOW$BOLD$rodTime"
        } else if(rodTime == 0f) {
            "$LIGHT_RED$BOLD!!!"
        } else {
            "$YELLOW${BOLD}3.0"
        }

        text.setText(string)
    }
}