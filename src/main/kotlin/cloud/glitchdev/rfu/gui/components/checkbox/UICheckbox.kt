package cloud.glitchdev.rfu.gui.components.checkbox

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.min
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import cloud.glitchdev.rfu.gui.components.Colorable

/**
 * Simple Checkbox Component
 */
class UICheckbox(
    val text: String,
    defaultState: Boolean = false,
    val allowDisabling: Boolean = true,
    var onChange: (Boolean) -> Unit = {}
) : UIContainer(), Colorable {
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var hoverColor = UIScheme.secondaryColor.toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()
    val animationDuration = UIScheme.HOVER_EFFECT_DURATION
    val padding = 2f

    var state = defaultState
        set(value) {
            checkmark.setHidden(!value)
            field = value
        }
    lateinit var checkmark : UIText
    lateinit var checkbox : UIRoundedRectangle
    lateinit var textComponent : UIText

    init {
        create()
    }

    fun create() {
        checkbox = UIRoundedRectangle(2f).constrain {
            x = SiblingConstraint(padding)
            y = CenterConstraint()
            width = AspectConstraint(1f)
            height = min(80.percent(), 10.pixels())
            color = primaryColor
        } childOf this

        checkmark = UIText("✔").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = min(TextAspectConstraint(), FillConstraint())
            height = 80.percent()
            color = textColor
        } childOf checkbox

        checkmark.setHidden(!state)

        this.onMouseClick {
            if(!allowDisabling && state) return@onMouseClick
            state = !state
            onChange(state)
        }.onMouseEnter {
            checkbox.animate {
                setColorAnimation(Animations.IN_EXP, animationDuration, hoverColor)
            }
        }.onMouseLeave {
            checkbox.animate {
                setColorAnimation(Animations.IN_EXP, animationDuration, primaryColor)
            }
        }

        textComponent = UIText(text).constrain {
            x = SiblingConstraint(padding)
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = ScaledTextConstraint(1f)
            color = textColor
        } childOf this
    }

    override fun refreshColors() {
        if (::checkbox.isInitialized) checkbox.constrain { color = primaryColor }
        if (::checkmark.isInitialized) checkmark.constrain { color = textColor }
        if (::textComponent.isInitialized) textComponent.constrain { color = textColor }
    }
}