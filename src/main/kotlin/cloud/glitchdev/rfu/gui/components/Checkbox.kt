package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.ColorScheme
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.min
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class Checkbox(val text: String, defaultState : Boolean = false, val callback : (Boolean) -> Unit = {}) : UIContainer() {
    val primaryColor = ColorScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = ColorScheme.secondaryColor.toConstraint()
    val textColor = ColorScheme.primaryTextColor.toConstraint()
    val padding = 2f
    val animationDuration = 0.1f

    var state = false

    init {
        state = defaultState
        create()
    }

    fun create() {
        val checkbox = UIRoundedRectangle(2f).constrain {
            x = SiblingConstraint(padding)
            y = CenterConstraint()
            width = AspectConstraint(1f)
            height = min(80.percent(), 10.pixels())
            color = primaryColor
        } childOf this

        val checkmark = UIText("✔").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint(), FillConstraint())
            height = 80.percent()
            color = textColor
        } childOf checkbox

        checkmark.hide()

        checkbox.onMouseClick {
            state = !state
            if(state) {
                checkmark.unhide()
            }
            else {
                checkmark.hide()
            }
            callback(state)
        }.onMouseEnter {
            checkbox.animate {
                setColorAnimation(Animations.IN_EXP, animationDuration, hoverColor)
            }
        }.onMouseLeave {
            checkbox.animate {
                setColorAnimation(Animations.IN_EXP, animationDuration, primaryColor)
            }
        }

        UIText(text).constrain {
            x = SiblingConstraint(padding)
            y = CenterConstraint()
            width = min(TextAspectConstraint(), FillConstraint()) - 5.pixels() - padding.pixels()
            height = ScaledTextConstraint(1f)
            color = textColor
        } childOf this
    }
}