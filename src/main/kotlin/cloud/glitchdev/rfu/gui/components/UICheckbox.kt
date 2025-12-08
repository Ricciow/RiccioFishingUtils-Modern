package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
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

/**
 * Simple Checkbox Component
 */
class UICheckbox(val text: String, defaultState : Boolean = false, val callback : (Boolean) -> Unit = {}) : UIContainer() {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val animationDuration = UIScheme.HOVER_EFFECT_DURATION
    val padding = 2f

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
        }.addHoverColoring(Animations.IN_EXP, animationDuration, primaryColor, hoverColor)

        UIText(text).constrain {
            x = SiblingConstraint(padding)
            y = CenterConstraint()
            width = min(TextAspectConstraint(), FillConstraint()) - 5.pixels() - padding.pixels()
            height = ScaledTextConstraint(1f)
            color = textColor
        } childOf this
    }
}