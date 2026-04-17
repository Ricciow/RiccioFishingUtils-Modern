package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import cloud.glitchdev.rfu.model.data.DataOption
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIConditionCard(
    val option: DataOption
) : UIRoundedRectangle(5f) {
    val innerColor: ColorConstraint
        get() {
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWater
                "lava" -> UIScheme.pfConditionCardLava
                "has_killer" -> UIScheme.pfConditionCardKiller
                "looting_5" -> UIScheme.pfConditionCardLooting5
                "enderman_9" -> UIScheme.pfConditionCardEnderman9
                "brain_food" -> UIScheme.pfConditionCardBrainFood
                "location" -> UIScheme.getIslandColor(option.label)
                else -> UIScheme.pfConditionCardUnknown
            }.toConstraint()
        }

    val borderColor: ColorConstraint
        get() {
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWaterBorder
                "lava" -> UIScheme.pfConditionCardLavaBorder
                "has_killer" -> UIScheme.pfConditionCardKillerBorder
                "looting_5" -> UIScheme.pfConditionCardLooting5Border
                "enderman_9" -> UIScheme.pfConditionCardEnderman9Border
                "brain_food" -> UIScheme.pfConditionCardBrainFoodBorder
                "location" -> UIScheme.getIslandBorderColor(option.label)
                else -> UIScheme.pfConditionCardUnknownBorder
            }.toConstraint()
        }

    val textHeight: HeightConstraint = ScaledTextConstraint(0.5f)
    val icon: String
        get() {
            return when(option.value as? String) {
                "water" -> "/assets/rfu/ui/water.png"
                "lava" -> "/assets/rfu/ui/lava.png"
                "has_killer" -> "/assets/rfu/ui/has_killer.png"
                "looting_5" -> "/assets/rfu/ui/looting_5.png"
                "enderman_9" -> "/assets/rfu/ui/enderman_9.png"
                "brain_food" -> "/assets/rfu/ui/brain_food.png"
                "location" -> "/assets/rfu/ui/location.png"
                else -> "/assets/rfu/ui/unknown.png"
            }
        }

    val borderWidth: PositionConstraint = UIScheme.pfConditionCardWidth.pixels
    val innerPadding: PositionConstraint = UIScheme.pfConditionCardPadding.pixels

    val background = UIRoundedRectangle(5f).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint() + innerPadding * 2
        height = ChildBasedMaxSizeConstraint() + innerPadding * 2
        color = innerColor
    } childOf this

    val innerContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedMaxSizeConstraint()
    } childOf background

    lateinit var textUI: UIText

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = borderColor
            width = ChildBasedSizeConstraint() + borderWidth * 2
            height = ChildBasedMaxSizeConstraint() + borderWidth * 2
        }

        val image = UIImage.ofResource(icon) childOf innerContainer

        textUI = UIText(option.label).constrain {
            x = SiblingConstraint(UIScheme.pfConditionCardPadding)
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = textHeight
            color = borderColor
        } childOf innerContainer

        image.constrain {
            x = SiblingConstraint()
            y = CenterConstraint()
            width = AspectConstraint()
            height = 4.5.pixels
            color = borderColor
        }
    }
}