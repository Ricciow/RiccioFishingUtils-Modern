package cloud.glitchdev.rfu.gui.components.textinput

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
import gg.essential.universal.UMatrixStack

class UIDecoratedTextInput(val placeholder : String, radius : Float, val numberOnly: Boolean = false, val maxChars : Int = 0, var onChange : (String) -> Unit = {}) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION

    private var textChanged = false
    private val numberRegex = "[^0-9]".toRegex()

    lateinit var textInput : UISpecialTextInput

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }
        this.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        textInput = (UISpecialTextInput(placeholder).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
            color = textColor
        }.onMouseClick {
            grabWindowFocus()
        }.onKeyType { _, _ ->
            textChanged = true
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

    fun setText(text : String) {
        textInput.setText(text)
        textChanged = true
    }

    fun getText() : String {
        return textInput.getText()
    }
}