package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.ColorScheme
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.min
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.dsl.minus

class Button(val text: String, radius: Float = 0f, val callback : () -> Unit) : UIRoundedRectangle(radius) {
    val primaryColor = ColorScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = ColorScheme.secondaryColor.toConstraint()
    val textColor = ColorScheme.primaryTextColor.toConstraint()
    val secondaryTextColor = ColorScheme.secondaryTextColor.toConstraint()
    val hoverDuration = 0.2f
    val clickDuration = 0.1f

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        val text = UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint() - 5.pixels(), 90.percent())
            height = 90.percent() - 5.pixels()
            color = textColor
        } childOf this

        this.onMouseClick {
            callback()
        }
        .onMouseEnter {
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
            }
        }
        .onMouseLeave {
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor)
            }
        }
        .onMouseClick {
            text.animate {
                setColorAnimation(Animations.IN_EXP, clickDuration, secondaryTextColor)
            }
        }
        .onMouseRelease {
            text.animate {
                setColorAnimation(Animations.IN_EXP, clickDuration, textColor)
            }
        }

    }
}