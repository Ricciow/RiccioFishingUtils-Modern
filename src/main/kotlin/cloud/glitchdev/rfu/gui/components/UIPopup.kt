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

class UIPopup(val radiusPopup : Float, val text: String) : UIBlock() {
    val backgroundColor = UIScheme.increaseOpacity(Color.BLACK, 128).toConstraint()
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()

    lateinit var uiText : UIWrappedText

    init {
        create()
        this.hide()
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
        } childOf container

        UIButton("Ok", 5f) {
            this.hide()
        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = 20.pixels()
        } childOf container
    }

    fun showPopup() {
        this.unhide()
    }
}