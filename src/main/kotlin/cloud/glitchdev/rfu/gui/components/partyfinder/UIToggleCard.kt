package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
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
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIToggleCard(
    val option: DataOption,
    initialState: Boolean = false,
    var onToggle: (Boolean) -> Unit = {}
) : UIRoundedRectangle(5f) {

    var selected: Boolean = initialState
        set(value) {
            if (field == value) return
            field = value
            updateState()
        }

    private val innerColor: ColorConstraint
        get() {
            if (!selected) return UIScheme.pfCardBg.toConstraint()
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWater
                "lava" -> UIScheme.pfConditionCardLava
                "has_killer" -> UIScheme.pfConditionCardKiller
                "looting_5" -> UIScheme.pfConditionCardLooting5
                "enderman_9" -> UIScheme.pfConditionCardEnderman9
                "brain_food" -> UIScheme.pfConditionCardBrainFood
                "can_join" -> UIScheme.pfConditionCardCanJoin
                "location" -> UIScheme.getIslandColor(option.label)
                else -> UIScheme.pfConditionCardUnknown
            }.toConstraint()
        }

    private val borderColor: ColorConstraint
        get() {
            if (!selected) return UIScheme.pfCardBorder.toConstraint()
            return when(option.value as? String) {
                "water" -> UIScheme.pfConditionCardWaterBorder
                "lava" -> UIScheme.pfConditionCardLavaBorder
                "has_killer" -> UIScheme.pfConditionCardKillerBorder
                "looting_5" -> UIScheme.pfConditionCardLooting5Border
                "enderman_9" -> UIScheme.pfConditionCardEnderman9Border
                "brain_food" -> UIScheme.pfConditionCardBrainFoodBorder
                "can_join" -> UIScheme.pfConditionCardCanJoinBorder
                "location" -> UIScheme.getIslandBorderColor(option.label)
                else -> UIScheme.pfConditionCardUnknownBorder
            }.toConstraint()
        }

    private val textColor: ColorConstraint
        get() {
            if (!selected) return Color(100, 100, 100).toConstraint()
            return borderColor
        }

    val textHeight: HeightConstraint = ScaledTextConstraint(0.7f)
    val icon: String
        get() {
            return when(option.value as? String) {
                "water" -> "/assets/rfu/ui/water.png"
                "lava" -> "/assets/rfu/ui/lava.png"
                "has_killer" -> "/assets/rfu/ui/has_killer.png"
                "looting_5" -> "/assets/rfu/ui/looting_5.png"
                "enderman_9" -> "/assets/rfu/ui/enderman_9.png"
                "brain_food" -> "/assets/rfu/ui/brain_food.png"
                "can_join" -> "/assets/rfu/ui/can_join.png"
                "location" -> "/assets/rfu/ui/location.png"
                "candy" -> "/assets/rfu/ui/candy.png"
                "unique" -> "/assets/rfu/ui/looting_5.png"
                else -> "/assets/rfu/ui/unknown.png"
            }
        }

    private val borderWidth: PositionConstraint = UIScheme.pfConditionCardWidth.pixels
    private val innerPadding: PositionConstraint = UIScheme.pfConditionCardPadding.pixels + 2.pixels

    private lateinit var background: UIRoundedRectangle
    private lateinit var innerContainer: UIContainer
    private lateinit var textUI: UIText
    private lateinit var iconUI: UIImage

    init {
        create()
    }

    private fun create() {
        this.constrain {
            color = borderColor
            width = ChildBasedSizeConstraint() + borderWidth * 2
            height = ChildBasedMaxSizeConstraint() + borderWidth * 2
        }

        background = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ChildBasedSizeConstraint() + innerPadding * 2
            height = ChildBasedMaxSizeConstraint() + innerPadding * 2
            color = innerColor
        } childOf this

        innerContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        } childOf background

        iconUI = UIImage.ofResource(icon).constrain {
            x = SiblingConstraint()
            y = CenterConstraint()
            width = AspectConstraint()
            height = 6.pixels
            color = textColor
        } childOf innerContainer

        textUI = UIText(option.label.uppercase()).constrain {
            x = SiblingConstraint(UIScheme.pfConditionCardPadding)
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = textHeight
            color = textColor
        } childOf innerContainer

        this.onMouseClick {
            selected = !selected
            onToggle(selected)
        }

        this.onMouseEnter {
            if (!selected) {
                this.animate {
                    setColorAnimation(Animations.OUT_QUAD, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
                }
            }
        }.onMouseLeave {
            if (!selected) {
                this.animate {
                    setColorAnimation(Animations.OUT_QUAD, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
                }
            }
        }
    }

    private fun updateState() {
        val duration = UIScheme.pfAnimationDuration
        val animType = Animations.OUT_QUAD

        this.animate {
            setColorAnimation(animType, duration, borderColor)
        }
        background.animate {
            setColorAnimation(animType, duration, innerColor)
        }
        textUI.animate {
            setColorAnimation(animType, duration, textColor)
        }
        iconUI.animate {
            setColorAnimation(animType, duration, textColor)
        }
    }
}
