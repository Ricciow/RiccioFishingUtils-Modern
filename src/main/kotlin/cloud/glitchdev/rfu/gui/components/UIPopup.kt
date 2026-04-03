package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIPopup(val radiusPopup : Float, val text: String, val onConfirm: (() -> Unit)? = null) : UIBlock() {
    val backgroundColor = UIScheme.increaseOpacity(Color.BLACK, 128).toConstraint()
    val errorColor = UIScheme.errorPopupColor.toConstraint()
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()

    lateinit var uiText : UIWrappedText

    init {
        create()
        this.hide()
    }

    fun setText(text : String) {
        uiText.setText(text)
    }

    fun create() {
        this.constrain {
            x = RelativeWindowConstraint(0f)
            y = RelativeWindowConstraint(0f)
            width = RelativeWindowConstraint(1f)
            height = RelativeWindowConstraint(1f)
            color = backgroundColor
            isFloating = true
        }

        this.onMouseClick {
            it.stopPropagation()
        }

        val popupContainer = UIRoundedRectangle(radiusPopup).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 33.percent()
            height = 33.percent()
            color = primaryColor
        } childOf this

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
        } childOf popupContainer

        uiText = UIWrappedText(text).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = FillConstraint()
            color = errorColor
        } childOf container

        if (onConfirm == null) {
            UIButton("Ok", 5f) {
                this.hide()
            }.constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = 100.percent()
                height = 20.pixels()
            } childOf container
        } else {
            val buttonContainer = UIContainer().constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = 100.percent()
                height = 20.pixels()
            } childOf container

            UIButton("Confirm", 5f) {
                onConfirm.invoke()
                this.hide()
            }.constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = 45.percent()
                height = 100.percent()
            } childOf buttonContainer

            UIButton("Cancel", 5f) {
                this.hide()
            }.constrain {
                x = 0.pixels(true)
                y = CenterConstraint()
                width = 45.percent()
                height = 100.percent()
            } childOf buttonContainer
        }
    }

    fun showPopup() {
        this.unhide()
    }
}