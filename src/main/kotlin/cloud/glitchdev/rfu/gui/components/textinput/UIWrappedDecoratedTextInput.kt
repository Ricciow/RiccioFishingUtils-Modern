package cloud.glitchdev.rfu.gui.components.textinput

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
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

class UIWrappedDecoratedTextInput(val placeholder : String, radius : Float, val maxChars : Int = 0) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION

    private var textChanged = false

    lateinit var textInput : UIMultilineTextInput

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }
        this.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        textInput = (UIMultilineTextInput(placeholder).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(90.percent(), 100.percent() - 5.pixels())
            height = max(90.percent(), 100.percent() - 5.pixels())
            color = textColor
        }.onMouseClick {
            grabWindowFocus()
        }.onKeyType { _, _ ->
            textChanged = true
        } childOf this) as UIMultilineTextInput
    }

    override fun draw(matrixStack: UMatrixStack) {
        if(textChanged) {
            val text = textInput.getText()
            if(maxChars != 0 && text.length > maxChars) {
                textInput.setText(text.slice(IntRange(0, maxChars-1)))
            }
            textChanged = false
        }

        super.draw(matrixStack)
    }
}