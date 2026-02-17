package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

@HudElement
object RodTimerDisplay : AbstractHudElement("rodTimer") {
    var rodTime : Float = -1f

    override val enabled: Boolean
        get() = GeneralFishing.rodTimerDisplay && (super.enabled || rodTime >= 0)

    var textContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    var text : UIText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(scale)
        height = TextAspectConstraint()
    } childOf textContainer

    override fun onInitialize() {
        text.setText("Rod Timer")
    }

    override fun onUpdateState() {
        text.constrain {
            width = ScaledTextConstraint(scale)
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