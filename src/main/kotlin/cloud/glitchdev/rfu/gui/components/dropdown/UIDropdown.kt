package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
import cloud.glitchdev.rfu.utils.dsl.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.min
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint

class UIDropdown(val values : ArrayList<DropdownOption>, var selectedIndex : Int = 0, val radiusProps : Float, val window: Window, val onSelect : (Any) -> Unit = {}) : UIContainer() {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val padding = 2f

    var isOpen = false
    //var selectedIndex
    lateinit var background : UIRoundedRectangle
    lateinit var textContainer : UIContainer
    lateinit var text : UIText
    lateinit var options : UIContainer

    init {
        create()
        updateDropdown()
    }

    fun create() {
        background = UIRoundedRectangle(radiusProps).constrain {
            y = 0.pixels()
            x = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
            color = primaryColor
            isFloating = true
        } childOf this

        background.isFloating = true

        background.onMouseClick {
            isOpen = !isOpen
            updateDropdown()
        }

        textContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 10.pixels()
        } childOf background

        text = UIText("").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint() - 5.pixels(), 90.percent())
            height = 100.percent() - 5.pixels()
            color = textColor
        } childOf textContainer

        options = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(padding)
            width = 100.percent() - 5.pixels()
            height = ChildBasedSizeConstraint()
        } childOf background

        for((index, option) in values.withIndex()) {
            createOption(option, index)
        }

        options.hide()
    }

    fun createOption(option : DropdownOption, index : Int) {
        val uiOption = UIRoundedRectangle(radiusProps).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(padding)
            width = 100.percent()
            height = 10.pixels()
            color = primaryColor
        } childOf options

        UIText(option.label).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint() - 5.pixels(), 90.percent())
            height = 90.percent() - 5.pixels()
            color = textColor
        } childOf uiOption

        uiOption.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        uiOption.onMouseClick {
            selectedIndex = index
            onSelect(option.value)
            updateDropdown()
        }
    }

    private fun getSelectedOption() : DropdownOption {
        return (values.getOrNull(selectedIndex) ?: DropdownOption("Dropdown", "Dropdown"))
    }

    fun updateDropdown() {
        textContainer.setHeight(this.getHeight().pixels())

        options.setHidden(!isOpen)
        if(isOpen) {
            val oldY = textContainer.getTop() - background.getTop()
            textContainer.constrain {
                y = SiblingConstraint(padding) + oldY.pixels()
            }
            background.constrain {
                height = ChildBasedSizeConstraint(padding) + oldY.pixels()
            }
        }
        else {
            textContainer.constrain {
                y = CenterConstraint()
            }
            background.constrain {
                height = 100.percent()
            }
        }

        text.setText(getSelectedOption().label)
    }
}