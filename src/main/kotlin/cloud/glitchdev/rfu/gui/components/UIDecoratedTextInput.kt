package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.UISpecialTextInput
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.toConstraint

class UIDecoratedTextInput(val placeholder : String, radius : Float) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }
        this.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        UISpecialTextInput(placeholder).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
            color = textColor
        }.onMouseClick {
            grabWindowFocus()
        } childOf this
    }
}