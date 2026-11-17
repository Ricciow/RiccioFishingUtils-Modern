package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.RFUFitText
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.utils.gui.UIImages
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.basicRadiusConstraint
import gg.essential.elementa.dsl.basicWidthConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint

class UIConditionCard(
    val option: DataOption
) : UIRoundedRectangle(5f) {
    val innerColor: ColorConstraint
        get() {
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWater
                "lava" -> UIScheme.pfConditionCardLava
                "hotspot" -> UIScheme.pfConditionCardHotspot
                "treasure" -> UIScheme.pfConditionCardTreasure
                "trophy" -> UIScheme.pfConditionCardTrophy
                "has_killer" -> UIScheme.pfConditionCardKiller
                "wither_impact" -> UIScheme.pfConditionCardWitherImpact
                "bobbin_time" -> UIScheme.pfConditionCardBobbinTime
                "enderman_9" -> UIScheme.pfConditionCardEnderman9
                "brain_food" -> UIScheme.pfConditionCardBrainFood
                "bloodshot" -> UIScheme.pfConditionCardBloodshot
                "location" -> UIScheme.getIslandColor(option.label)
                else -> UIScheme.pfConditionCardUnknown
            }.toConstraint()
        }

    val borderColor: ColorConstraint
        get() {
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWaterBorder
                "lava" -> UIScheme.pfConditionCardLavaBorder
                "hotspot" -> UIScheme.pfConditionCardHotspotBorder
                "treasure" -> UIScheme.pfConditionCardTreasureBorder
                "trophy" -> UIScheme.pfConditionCardTrophyBorder
                "has_killer" -> UIScheme.pfConditionCardKillerBorder
                "bobbin_time" -> UIScheme.pfConditionCardBobbinTimeBorder
                "wither_impact" -> UIScheme.pfConditionCardWitherImpactBorder
                "enderman_9" -> UIScheme.pfConditionCardEnderman9Border
                "brain_food" -> UIScheme.pfConditionCardBrainFoodBorder
                "bloodshot" -> UIScheme.pfConditionCardBloodshotBorder
                "location" -> UIScheme.getIslandBorderColor(option.label)
                else -> UIScheme.pfConditionCardUnknownBorder
            }.toConstraint()
        }

    val icon: String
        get() {
            return when(option.value as? String) {
                "water" -> "/assets/rfu/ui/water.png"
                "lava" -> "/assets/rfu/ui/lava.png"
                "hotspot" -> "/assets/rfu/ui/location.png"
                "treasure" -> "/assets/rfu/ui/treasure.png"
                "trophy" -> "/assets/rfu/ui/trophy.png"
                "has_killer" -> "/assets/rfu/ui/has_killer.png"
                "bobbin_time" -> "/assets/rfu/ui/bobbin_time.png"
                "wither_impact" -> "/assets/rfu/ui/wither_impact.png"
                "enderman_9" -> "/assets/rfu/ui/enderman_9.png"
                "brain_food" -> "/assets/rfu/ui/brain_food.png"
                "bloodshot" -> "/assets/rfu/ui/water.png"
                "location" -> "/assets/rfu/ui/location.png"
                else -> "/assets/rfu/ui/unknown.png"
            }
        }

    val borderWidth: PositionConstraint = UIScheme.pfConditionCardWidth.pixels
    val innerPadding: PositionConstraint = UIScheme.pfConditionCardPadding.pixels

    val background = UIRoundedRectangle(5f).constrain {
        radius = basicRadiusConstraint { it.getHeight() / 2f }
        x = CenterConstraint()
        y = CenterConstraint()
        width = CopyComponentSizeConstraint(this@UIConditionCard) - borderWidth * 2
        height = CopyComponentSizeConstraint(this@UIConditionCard) - borderWidth * 2
        color = innerColor
    } childOf this

    val innerContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = basicWidthConstraint {
            UIScheme.pfConditionCardContentHeight + UIScheme.pfConditionCardPadding + textUI.getWidth()
        }
        height = UIScheme.pfConditionCardContentHeight.pixels
    } childOf background

    lateinit var textUI: RFUFitText

    init {
        create()
    }

    fun create() {
        this.constrain {
            radius = basicRadiusConstraint { it.getHeight() / 2f }
            color = borderColor
            width = CopyComponentSizeConstraint(innerContainer) + (innerPadding + borderWidth) * 2
            height = UIScheme.pfConditionCardContentHeight.pixels + (innerPadding + borderWidth) * 2
        }

        val image = UIImages.ofResource(icon) childOf innerContainer

        textUI = RFUFitText(option.label.trim(), UIScheme.pfConditionCardTextScale).constrain {
            x = (UIScheme.pfConditionCardContentHeight + UIScheme.pfConditionCardPadding).pixels
            y = CenterConstraint()
            width = basicWidthConstraint { component ->
                (component as RFUFitText).getTextWidth() * UIScheme.pfConditionCardTextScale
            }
            color = borderColor
        } childOf innerContainer

        image.constrain {
            x = 0.pixels
            y = CenterConstraint()
            width = UIScheme.pfConditionCardContentHeight.pixels
            height = UIScheme.pfConditionCardContentHeight.pixels
            color = borderColor
        }
    }
}
