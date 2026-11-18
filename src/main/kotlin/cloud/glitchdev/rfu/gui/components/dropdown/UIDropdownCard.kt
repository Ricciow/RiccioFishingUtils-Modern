package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.utils.gui.UIImages
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.basicWidthConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIDropdownCard(
    values: ArrayList<DataOption>,
    private val inactiveLabel: String,
    iconResource: String? = null,
    private val activeBackgroundColor: Color = UIScheme.pfConditionCardUnknown,
    private val activeBorderColor: Color = UIScheme.pfConditionCardUnknownBorder,
    initialIndex: Int = 0,
    private val inactiveIndex: Int = 0,
    onSelect: (DataOption) -> Unit = {}
) : UIDropdown(values, initialIndex, radiusProps = 5f, hideArrow = true, onSelect = onSelect) {
    private val borderWidth = UIScheme.pfConditionCardWidth
    private val cardPadding = UIScheme.pfConditionCardPadding + 2f
    private val labelScale = 0.7f
    private val inactiveTextColor = Color(100, 100, 100)
    private var cardReady = false
    private var textReady = false

    private val frame = UIRoundedRectangle(5f).constrain {
        x = (-borderWidth).pixels
        y = (-borderWidth).pixels
        width = 100.percent + (borderWidth * 2).pixels
        height = 100.percent + (borderWidth * 2).pixels
        color = UIScheme.pfCardBorder.toConstraint()
    }

    init {
        UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - (borderWidth * 2).pixels
            height = 100.percent - (borderWidth * 2).pixels
            color = UIScheme.pfCardBg.toConstraint()
        } childOf frame
    }

    private val headerBackground = UIRoundedRectangle(5f).constrain {
        width = 100.percent
        height = 100.percent
        color = UIScheme.transparent.toConstraint()
    }

    private val headerContent = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = 100.percent
    } childOf textContainer

    private val icon = iconResource?.let { resource ->
        UIImages.ofResource(resource).constrain {
            x = 0.pixels
            y = CenterConstraint()
            width = 6.pixels
            height = 6.pixels
            color = inactiveTextColor.toConstraint()
        } childOf headerContent
    }

    init {
        background.insertChildAt(frame, 0)
        textContainer.insertChildAt(headerBackground, 0)
        textContainer.removeChild(text)
        text childOf headerContent

        primaryColor = UIScheme.transparent.toConstraint()
        headerHoverColor = UIScheme.transparent.toConstraint()
        hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
        optionColor = UIScheme.pfDropdownOptionBg.toConstraint()
        selectedColor = activeBackgroundColor.toConstraint()
        autoFontSize = false
        maxFontSize = labelScale
        fontSize = labelScale

        text.constrain {
            x = if (icon == null) 0.pixels else SiblingConstraint(UIScheme.pfConditionCardPadding)
            y = CenterConstraint()
        }
        configureSize()

        listOf(this, frame).forEach { component ->
            component.onMouseEnter {
                if (selectedIndex == inactiveIndex) setBorderColor(UIScheme.pfCardBorderHovered, UIScheme.HOVER_EFFECT_DURATION)
            }.onMouseLeave {
                if (selectedIndex == inactiveIndex && !this@UIDropdownCard.isHovered() && !frame.isHovered()) {
                    setBorderColor(UIScheme.pfCardBorder, UIScheme.HOVER_EFFECT_DURATION)
                }
            }
        }

        cardReady = true
        updateDropdownState()
    }

    private fun configureSize() {
        constrain {
            width = basicWidthConstraint {
                maxOf(
                    headerContent.getWidth() + cardPadding * 2 + borderWidth * 2,
                    (uiOptionsText.maxOfOrNull { it.getWidth() } ?: 0f) + cardPadding * 3 + borderWidth * 2
                )
            }
            height = (9f * labelScale + (cardPadding + borderWidth) * 2).pixels
        }
    }

    override fun afterInitialization() {
        super.afterInitialization()
        initializeText()
        textReady = true
    }

    override fun setValues(newValues: List<DataOption>) {
        super.setValues(newValues)
        if (textReady) initializeText()
    }

    private fun initializeText() {
        val header = text.getText()
        headerContent.removeChild(text)
        text = UIText(header).constrain {
            x = if (icon == null) 0.pixels else SiblingConstraint(UIScheme.pfConditionCardPadding)
            y = CenterConstraint()
            textScale = labelScale.pixels
        } childOf headerContent

        uiOptions.forEachIndexed { index, option ->
            option.removeChild(uiOptionsText[index])
            uiOptionsText[index] = UIText(values[index].label).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                textScale = labelScale.pixels
                color = UIScheme.primaryTextColor.toConstraint()
            } childOf option
        }
        headerContent.constrain { width = ChildBasedSizeConstraint() }
        configureSize()
        updateDropdownState()
    }

    override fun updateHeight() {
        if (!cardReady) {
            super.updateHeight()
        } else {
            textContainer.constrain {
                height = (this@UIDropdownCard.getHeight() - borderWidth * 2).pixels
            }
        }
    }

    override fun updateDropdownState() {
        super.updateDropdownState()
        if (!cardReady) return

        background.constrain {
            x = CenterConstraint()
            y = borderWidth.pixels
            width = 100.percent - (borderWidth * 2).pixels
            if (!isOpen) height = 100.percent - (borderWidth * 2).pixels
        }
        updateHeight()

        val active = selectedIndex != inactiveIndex && selectedIndex in values.indices
        val borderColor = if (active) activeBorderColor else UIScheme.pfCardBorder
        val foregroundColor = if (active) activeBorderColor else inactiveTextColor
        text.setText(if (active) getSelectedItem().label else inactiveLabel)
        textColor = foregroundColor.toConstraint()
        refreshColors()
        uiOptionsText.forEach { optionText ->
            optionText.constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                color = UIScheme.primaryTextColor.toConstraint()
            }
        }
        setBorderColor(borderColor, UIScheme.pfAnimationDuration)
        setColor(headerBackground, if (active) activeBackgroundColor else UIScheme.transparent, UIScheme.pfAnimationDuration)
        icon?.let { setColor(it, foregroundColor, UIScheme.pfAnimationDuration) }
    }

    private fun setBorderColor(color: Color, duration: Float) = setColor(frame, color, duration)

    private fun setColor(component: UIComponent, targetColor: Color, duration: Float) {
        if (Window.ofOrNull(component) == null) {
            component.constrain { color = targetColor.toConstraint() }
        } else {
            component.animate {
                setColorAnimation(Animations.OUT_QUAD, duration, targetColor.toConstraint())
            }
        }
    }
}
