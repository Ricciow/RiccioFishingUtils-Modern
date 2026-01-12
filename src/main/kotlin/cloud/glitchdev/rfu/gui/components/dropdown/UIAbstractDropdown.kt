package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.utils.dsl.height
import cloud.glitchdev.rfu.utils.dsl.setHidden
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import kotlin.math.min

abstract class UIAbstractDropdown(
    val values: ArrayList<DataOption>,
    val radiusProps: Float,
    val hideArrow: Boolean = false,
    val label: String = ""
) : UIContainer() {

    protected val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    protected val hoverColor = UIScheme.secondaryColor.toConstraint()
    protected val textColor = UIScheme.primaryTextColor.toConstraint()
    protected val selectedColor = UIScheme.primaryColor.toConstraint()
    protected val disabledColor = UIScheme.secondaryColorDisabled.toConstraint()
    protected val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    protected val padding = 2f
    protected val optionHeightPixels = 12f

    var isOpen = false

    lateinit var background: UIRoundedRectangle
    lateinit var textContainer: UIContainer
    lateinit var text: UIText
    lateinit var arrowHead: UIText
    lateinit var listContainer: UIContainer
    lateinit var scrollComponent: ScrollComponent
    lateinit var scrollbar: UIRoundedRectangle

    val uiOptions: MutableList<UIRoundedRectangle> = mutableListOf()
    val uiOptionsText: MutableList<UIText> = mutableListOf()

    abstract fun onOptionClicked(option: DataOption, index: Int)
    abstract fun isOptionSelected(index: Int): Boolean
    abstract fun getDropdownDisplayText(): String

    var fontSize: Float = 1.5f
        set(value) {
            field = value
            if (::text.isInitialized) {
                text.constrain { width = ScaledTextConstraint(value) }
                arrowHead.constrain { width = ScaledTextConstraint(value / 3 * 2) }
                uiOptionsText.forEach { it.constrain { width = ScaledTextConstraint(value) } }
            }
        }

    private var lastHeight = -1f

    init {
        create()
    }

    open fun setValues(newValues: List<DataOption>) {
        uiOptions.forEach { scrollComponent.removeChild(it) }

        uiOptions.clear()
        uiOptionsText.clear()

        this.values.clear()
        this.values.addAll(newValues)

        for ((index, option) in values.withIndex()) {
            createOptionUI(option, index)
        }

        updateDropdownState()

        updateFontSize()
    }

    open fun create() {

        background = UIRoundedRectangle(radiusProps).constrain {
            y = 0.pixels()
            x = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
            color = primaryColor
        } childOf this

        this.onMouseClick {
            background.grabWindowFocus()
            toggleDropdown()
        }

        background.onMouseClick { event ->
            if(event.absoluteY > this@UIAbstractDropdown.getBottom()) {
                event.stopPropagation()
                background.grabWindowFocus()
            }
        }.onFocusLost {
            isOpen = false
            updateDropdownState()
        }.onMouseEnter {
            if (!isOpen) this.animate { setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor) }
        }.onMouseLeave {
            if (!isOpen) this.animate { setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor) }
        }.onKeyType { _, code ->
            if(code == UKeyboard.KEY_ESCAPE) {
                background.releaseWindowFocus()
            }
        }

        textContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 10.pixels()
        } childOf background

        text = UIText(label).constrain {
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

        arrowHead.setHidden(hideArrow)

        listContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(padding)
            width = 90.percent()
            height = 0.pixels()
        } childOf background

        scrollbar = UIRoundedRectangle(radiusProps / 2).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            height = 100.percent()
            color = hoverColor
        } childOf listContainer

        scrollComponent = ScrollComponent().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent() - 5.pixels()
            height = 100.percent()
        } childOf listContainer

        scrollComponent.setScrollBarComponent(scrollbar, false, false)

        for ((index, option) in values.withIndex()) {
            createOptionUI(option, index)
        }

        listContainer.hide()
        updateDropdownState()
    }

    private fun createOptionUI(option: DataOption, index: Int) {
        val uiOption = UIRoundedRectangle(radiusProps).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = optionHeightPixels.pixels()
            color = primaryColor
        } childOf scrollComponent

        uiOptions.add(uiOption)

        val optionText = UIText(option.label).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ScaledTextConstraint(fontSize)
            height = TextAspectConstraint()
            color = textColor
        } childOf uiOption

        uiOptionsText.add(optionText)

        uiOption.onMouseEnter {
            if (shouldHover() || isOptionSelected(index)) {
                uiOption.animate { setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor) }
            }
        }.onMouseLeave {
            if (shouldHover() || isOptionSelected(index)) {
                val targetColor = if (isOptionSelected(index)) selectedColor else primaryColor
                uiOption.animate { setColorAnimation(Animations.IN_EXP, hoverDuration, targetColor) }
            }
        }

        uiOption.onMouseClick { event ->
            event.stopPropagation()
            background.grabWindowFocus()
            onOptionClicked(option, index)
        }
    }

    open fun shouldHover(): Boolean {
        return true
    }

    open fun isOptionDisabled(index: Int): Boolean {
        return false
    }

    fun toggleDropdown() {
        isOpen = !isOpen
        if (isOpen) {
            this.constrain { color = primaryColor }
        } else {
            this.animate { setColorAnimation(Animations.IN_SIN, hoverDuration * 2, hoverColor) }
        }
        updateDropdownState()
    }

    open fun updateDropdownState() {
        if (!::listContainer.isInitialized) return

        listContainer.setHidden(!isOpen)
        background.isFloating = isOpen

        if (isOpen) {
            val headerHeight = textContainer.getHeight()
            val contentHeight = values.size * (optionHeightPixels + padding) + padding
            val window = Window.of(this)
            val dropdownStartY = this.getTop() + headerHeight + padding
            val availableHeight = window.getHeight() - dropdownStartY - 10f
            val finalHeight = min(contentHeight, availableHeight.coerceAtLeast(optionHeightPixels))

            if(values.isNotEmpty()) {
                textContainer.constrain { y = SiblingConstraint() }

                listContainer.constrain {
                    height = finalHeight.pixels()
                    y = SiblingConstraint(padding) + (headerHeight - background.getHeight()).pixels()
                }

                background.constrain {
                    height = (headerHeight + padding + finalHeight + padding).pixels()
                    color = primaryColor
                }
            }

            arrowHead.setText("▲")
        } else {
            listContainer.constrain { height = 0.pixels() }
            textContainer.constrain { y = CenterConstraint() }
            background.constrain { height = 100.percent() }
            arrowHead.setText("▼")
        }

        if (label.isEmpty()) {
            text.setText(getDropdownDisplayText())
        }

        refreshOptionColors()
    }

    fun refreshOptionColors() {
        uiOptions.forEachIndexed { index, uiOption ->
            if (index < values.size) {
                val targetColor = if (isOptionSelected(index)) selectedColor else if (isOptionDisabled(index)) disabledColor else primaryColor
                uiOption.constrain { color = targetColor }
            }
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        val currentHeight = this.getHeight()
        if (lastHeight != currentHeight) {
            lastHeight = currentHeight
            updateHeight()
        }
        super.draw(matrixStack)
    }

    open fun updateHeight() {
        val newHeight = this.getHeight()
        if (::textContainer.isInitialized) {
            textContainer.setHeight(newHeight.pixels())
            updateFontSize()
        }
    }

    private fun updateFontSize() {
        if (values.isEmpty()) return
        var font = fontSize
        for (option in values) {
            while (option.label.height(font) < textContainer.getHeight() * 0.9) {
                font += 0.1f
            }
        }
        for (option in values) {
            while (option.label.height(font) > textContainer.getHeight() * 0.9 ||
                option.label.width(font) > textContainer.getWidth() * 0.8
            ) {
                font -= 0.1f
            }
        }
        fontSize = font
    }
}