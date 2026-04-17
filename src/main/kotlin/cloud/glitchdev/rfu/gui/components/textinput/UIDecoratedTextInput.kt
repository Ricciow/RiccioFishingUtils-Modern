package cloud.glitchdev.rfu.gui.components.textinput

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.UISpecialTextInput
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UMatrixStack
import cloud.glitchdev.rfu.gui.components.Colorable
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.pixels

class UIDecoratedTextInput(
    val placeholder: String,
    radius: Float,
    val numberOnly: Boolean = false,
    val maxChars: Int = 0,
    var onChange: (String) -> Unit = {}
) : UIRoundedRectangle(radius), Colorable {
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var hoverColor = UIScheme.secondaryColor.toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()
    var unselectedTextColor = UIScheme.placeholderTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    var isEnabled = true
        set(value) {
            field = value
            this.constrain {
                color = (if (value) primaryColor else UIScheme.disabledColor.toConstraint())
            }
            updateTextColor()
        }
    var isFocused = false
        private set
    private var textChanged = false
    private val numberRegex = "[^0-9]".toRegex()

    lateinit var textInput : UISpecialTextInput

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = if (isEnabled) primaryColor else UIScheme.disabledColor.toConstraint()
        }
        this.onMouseEnter {
            if (!isEnabled) return@onMouseEnter
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
            }
        }.onMouseLeave {
            if (!isEnabled) return@onMouseLeave
            this.animate {
                setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor)
            }
        }

        textInput = (UISpecialTextInput(placeholder).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 4.pixels
            height = 100.percent() - 2.pixels
            color = if (isEnabled) unselectedTextColor else UIScheme.disabledTextColor.toConstraint()
        }.onMouseClick {
            if (!isEnabled) return@onMouseClick
            grabWindowFocus()
        }.onKeyType { _, _ ->
            if (!isEnabled) return@onKeyType
            textChanged = true
        }.onFocus {
            isFocused = true
            updateTextColor()
        }.onFocusLost {
            isFocused = false
            updateTextColor()
        } childOf this) as UISpecialTextInput
    }

    override fun draw(matrixStack: UMatrixStack) {
        if(textChanged) {
            val text = textInput.getText()
            if(numberOnly && textInput.getText().contains(numberRegex)) {
                textInput.setText(numberRegex.replace(text, ""))
            }
            if(maxChars != 0 && text.length > maxChars) {
                textInput.setText(text.slice(IntRange(0, maxChars-1)))
            }
            onChange(textInput.getText())
            textChanged = false
        }

        super.draw(matrixStack)
    }

    fun setText(text : String, triggerOnChange: Boolean = false) {
        textInput.setText(text)
        textChanged = triggerOnChange
        updateTextColor()
    }

    fun getText() : String {
        return textInput.getText()
    }

    fun updateTextColor() {
        val targetColor = when {
            !isEnabled -> UIScheme.disabledTextColor.toConstraint()
            !isFocused && getText().isEmpty() -> unselectedTextColor
            else -> textColor
        }
        textInput.constrain {
            color = targetColor
        }
    }

    override fun refreshColors() {
        this.constrain { color = primaryColor }
        if (::textInput.isInitialized) {
            updateTextColor()
        }
    }
}