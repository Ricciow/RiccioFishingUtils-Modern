package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
import cloud.glitchdev.rfu.utils.dsl.height
import cloud.glitchdev.rfu.utils.dsl.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.dsl.width
import gg.essential.universal.UMatrixStack

/**
 * Dropdown component
 */
class UIDropdown(val values : ArrayList<DropdownOption>, var selectedIndex : Int = 0, val radiusProps : Float, val onSelect : (Any) -> Unit = {}) : UIContainer() {
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
    lateinit var arrowHead : UIText
    lateinit var options : UIContainer
    val uiOptions : MutableList<UIRoundedRectangle> = mutableListOf()
    val uiOptionsText : MutableList<UIText> = mutableListOf()

    var fontSize : Float = 1.5f
        set(value) {
            text.constrain {
                width = ScaledTextConstraint(value)
            }
            arrowHead.constrain {
                width = ScaledTextConstraint(value / 3 * 2)
            }
            for(optionText in uiOptionsText) {
                optionText.constrain {
                    width = ScaledTextConstraint(value)
                }
            }
        }

    private var lastHeight = this.getHeight()
    override fun draw(matrixStack: UMatrixStack) {
        val currentHeight = this.getHeight()
        if(lastHeight != this.getHeight()) {
            lastHeight = currentHeight
            updateHeight()
        }
        super.draw(matrixStack)
    }

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
            width = ScaledTextConstraint(fontSize)
            height = TextAspectConstraint()
            color = textColor
        } childOf textContainer

        arrowHead = UIText("▼").constrain {
            x = 5.pixels(true)
            y = CenterConstraint()
            width = ScaledTextConstraint(fontSize / 3 * 2)
            height = TextAspectConstraint()
            color = textColor
        } childOf textContainer

        options = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(padding)
            width = 90.percent()
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

        uiOptions.addLast(uiOption)

        val optionText = UIText(option.label).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ScaledTextConstraint(fontSize)
            height = TextAspectConstraint()
            color = textColor
        } childOf uiOption

        uiOptionsText.addLast(optionText)

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

    fun updateHeight() {
        val newHeight = this.getHeight()
        println("newheight: $newHeight")
        textContainer.setHeight(newHeight.pixels())
        for(uiOption in uiOptions) {
            uiOption.setHeight(newHeight.pixels())
        }
        updateFontSize()
    }

    fun updateFontSize() {
        var font = fontSize
        for(option in values) {
            while (option.label.height(font) < textContainer.getHeight() * 0.9) {
                font += 0.1f
                println("Fonte mudada $font")
            }
        }
        for(option in values) {
            while(option.label.height(font) > textContainer.getHeight() * 0.9 ||
                    option.label.width(font) > textContainer.getWidth() * 0.8)
            {
                font -= 0.1f
                println("Fonte mudada $font\nwidth:${option.label.width(font)}\nlf:${textContainer.getWidth() * 0.8}\nheight:${option.label.height(font)}\nlf${textContainer.getHeight() * 0.9}" )
            }
        }
        fontSize = font
    }

    fun updateDropdown() {
        options.setHidden(!isOpen)
        if(isOpen) {
            val oldY = textContainer.getTop() - background.getTop()
            textContainer.constrain {
                y = SiblingConstraint(padding) + oldY.pixels()
            }
            background.constrain {
                height = ChildBasedSizeConstraint(padding) + oldY.pixels()
            }
            arrowHead.setText("▲")
        }
        else {
            textContainer.constrain {
                y = CenterConstraint()
            }
            background.constrain {
                height = 100.percent()
            }
            arrowHead.setText("▼")
        }

        text.setText(getSelectedOption().label)
    }
}