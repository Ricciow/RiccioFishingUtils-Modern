package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*

class UIPartyBadge(val label: String, val value: String, val titleText: UIComponent) : UIRoundedRectangle(5f) {
    val valueText: UIText

    init {
        this.constrain {
            color = UIScheme.pfCardLevelBorderColor.toConstraint()
        }

        val innerContainer = UIRoundedRectangle(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - 2.pixels
            height = 100.percent - 2.pixels
            color = UIScheme.pfCardLevelBgColor.toConstraint()
        } childOf this

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        } childOf innerContainer

        UIText(label).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = TextAspectConstraint()
            height = CopyComponentSizeConstraint(titleText) * 0.45
            color = UIScheme.pfCardLevelLabelColor.toConstraint()
        } childOf container

        valueText = UIText(value).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(1f)
            width = TextAspectConstraint()
            height = CopyComponentSizeConstraint(titleText) * 0.65
        } childOf container
    }

    fun animateHover() {
        this.animate {
            setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardLevelBorderHoveredColor.toConstraint())
        }
        valueText.animate {
            setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
        }
    }

    fun animateNormal() {
        this.animate {
            setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardLevelBorderColor.toConstraint())
        }
        valueText.animate {
            setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleColor.toConstraint())
        }
    }
}
