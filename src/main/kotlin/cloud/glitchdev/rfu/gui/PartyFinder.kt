package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.partyfinder.UIFilterArea
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class PartyFinder : BaseWindow() {
    val primaryColor = UIScheme.primaryColorOpaque.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val radius = 5f
    val windowSize = 0.8f

    lateinit var background : UIRoundedRectangle

    init {
        create()
    }

    fun create() {

        background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(windowSize)
            height = RelativeWindowConstraint(windowSize)
            color = primaryColor
        } childOf window

        createHeader()

        UIFilterArea(radius).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = max(20.percent(), 40.pixels())
            color = primaryColor
        } childOf background

        Inspector(window) childOf window
    }

    fun createHeader() {
        val header = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = max(10.percent(), 20.pixels())
            color = primaryColor
        } childOf background

        UIText("RFU Party Finder").constrain {
            x = 2.percent()
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = 50.percent()
            color = textColor
        } childOf header

        val rightContainer = UIContainer().constrain {
            x = 98.percent()
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 80.percent()
        } childOf header

        val createButton = UIButton("New Party", 3f).constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = 70.pixels()
            height = 100.percent()
        } childOf rightContainer

        val filtersButton = UIButton("Filters", 3f).constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = 50.pixels()
            height = 100.percent()
        } childOf rightContainer

            rightContainer.constrain {
            x = 98.percent() - rightContainer.getWidth().pixels()
        }
    }
}