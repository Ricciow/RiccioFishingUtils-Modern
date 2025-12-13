package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
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

/**
 * Simple Button Component
 */
class UIButton(val text: String, radius: Float = 0f, val callback : () -> Unit = {}) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val secondaryTextColor = UIScheme.secondaryTextColor.toConstraint()
    val disabledColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val clickDuration = 0.1f

    var disabled = false
        set(value) {
            field = value
            this.constrain {
                color = if (disabled) disabledColor else primaryColor
            }
            textArea.constrain {
                color = if (disabled) secondaryTextColor else textColor
            }
        }

    lateinit var textArea : UIText

    init {
        create()
    }

    fun setText(text : String) {
        textArea.setText(text)
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        textArea = UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint() - 5.pixels(), 90.percent())
            height = 90.percent() - 5.pixels()
            color = textColor
        } childOf this

        this.onMouseClick {
            if(!disabled) {
                callback()
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, secondaryTextColor)
                }
            }
        }
        .onMouseRelease {
            if(!disabled) {
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, textColor)
                }
            }
        }
        .onMouseEnter {
            if(!disabled) {
                this.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
                }
            }
        }
        .onMouseLeave {
            if(!disabled) {
                this.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor)
                }
            }
        }

    }
}