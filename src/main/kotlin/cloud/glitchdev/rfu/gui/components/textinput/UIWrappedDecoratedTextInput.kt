package cloud.glitchdev.rfu.gui.components.textinput

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.gui.addHoverColoring
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UIMultilineTextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UMatrixStack
import cloud.glitchdev.rfu.gui.components.Colorable
import cloud.glitchdev.rfu.gui.components.elementa.UISpecialMultilineTextInput
import gg.essential.elementa.dsl.animate
import java.awt.Color

class UIWrappedDecoratedTextInput(
    val placeholder: String,
    radius: Float,
    val maxChars: Int = 0,
    var onChange: (String) -> Unit = {}
) : UIRoundedRectangle(radius), Colorable {
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var hoverColor = UIScheme.secondaryColor.toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()
    var unselectedTextColor = UIScheme.placeholderTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    var isFocused = false
        private set

    private var textChanged = false

    lateinit var textInput : UISpecialMultilineTextInput

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }
        this.onMouseEnter {
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
            }
        }.onMouseLeave {
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor)
            }
        }

        textInput = (UISpecialMultilineTextInput(placeholder).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(90.percent(), 100.percent() - 5.pixels())
            height = max(90.percent(), 100.percent() - 5.pixels())
        }.onMouseClick {
            grabWindowFocus()
        }.onKeyType { _, _ ->
            textChanged = true
        }.onFocus {
            isFocused = true
            updateTextColor()
        }.onFocusLost {
            isFocused = false
            updateTextColor()
        } childOf this) as UISpecialMultilineTextInput
    }

    override fun draw(matrixStack: UMatrixStack) {
        if(textChanged) {
            val text = textInput.getText()
            if(maxChars != 0 && text.length > maxChars) {
                textInput.setText(text.slice(IntRange(0, maxChars-1)))
            }
            onChange(textInput.getText())
            textChanged = false
        }

        super.draw(matrixStack)
    }

    fun setText(text : String) {
        textInput.setText(text)
        textChanged = true
        updateTextColor()
    }

    fun getText() : String {
        return textInput.getText()
    }

    fun updateTextColor() {
        if (::textInput.isInitialized) {
            if(!isFocused && getText().isEmpty()) {
                textInput.constrain {
                    color = unselectedTextColor
                }
            } else {
                textInput.constrain {
                    color = textColor
                }
            }
        }
    }

    override fun refreshColors() {
        this.constrain { color = primaryColor }
        updateTextColor()
    }
}