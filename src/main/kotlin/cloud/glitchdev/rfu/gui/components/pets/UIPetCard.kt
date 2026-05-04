package cloud.glitchdev.rfu.gui.components.pets

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.group.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.model.pets.PetAuctionResponse
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.gui.ColorUtils.toJavaColor
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import java.awt.Color
import kotlin.text.format

class UIPetCard(
    val pet: PetAuctionResponse,
    val radiusProps: Float
) : UIRoundedRectangle(radiusProps) {
    private val borderWidth = UIScheme.pfCardBorderWidth
    private val innerPadding = UIScheme.pfCardInnerPadding
    private val smallPadding = UIScheme.pfCardSmallPadding
    
    private var titleText: UIText = UIText("")

    init {
        create()
    }

    private fun create() {
        this.constrain {
            color = UIScheme.pfCardBorder.toConstraint()
            width = 100.percent
            height = GroupMaxSizeConstraint("PetCardMain", ChildBasedSizeConstraint() + borderWidth.pixels * 2)
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, pet.rarity.color.toJavaColor().toConstraint())
            }
        }.onMouseClick {
            if(UKeyboard.isCtrlKeyDown() || UKeyboard.isShiftKeyDown()) {
                mc.keyboardHandler.clipboard = "./viewauction ${pet.uuid} ([Lv${pet.level}] ${pet.petName}, ${pet.profit.formatLargeNumber()} profit, ${pet.coinsPerExp.formatSmallNumber()} coins/xp)"
                Chat.sendMessage(TextUtils.rfuLiteral("Copied pet info to clipboard!", TextColor.LIGHT_GREEN))
            } else {
                Chat.sendCommand("viewauction ${pet.uuid}")
            }
        }

        val innerBg = UIRoundedRectangle(radiusProps).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels
            width = 100.percent - (borderWidth * 2).pixels
            height = GroupMaxSizeConstraint("PetCardInnerBg", ChildBasedSizeConstraint() + innerPadding.pixels * 2)
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        val innerContainer = UIContainer().constrain {
            x = innerPadding.pixels
            y = innerPadding.pixels
            width = 100.percent - (innerPadding * 2).pixels
            height = GroupMaxSizeConstraint("PetCardInner", ChildBasedMaxSizeConstraint())
        } childOf innerBg

        val rightArea = UIContainer().constrain {
            x = 0.pixels
            y = 0.pixels
            width = FillConstraint()
            height = GroupMaxSizeConstraint("PetCardRight", ChildBasedSizeConstraint())
        } childOf innerContainer

        titleText = UIText("${TextColor.DARK_GRAY}[${TextColor.GRAY}Lv${pet.level}${TextColor.DARK_GRAY}] ${TextEffects.RESET}${pet.petName}").constrain {
            x = 0.pixels
            y = 0.pixels
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 9.pixels
            color = pet.rarity.color.toJavaColor().toConstraint()
        } childOf rightArea

        val subtitleText = if (pet.candyUsed > 0) {
            "${pet.category.color}${pet.category} ${TextColor.DARK_GRAY}• ${pet.rarity.color}${pet.rarity} ${TextColor.DARK_GRAY}• ${TextColor.MAGENTA}${pet.candyUsed} Candy Used"
        } else {
            "${pet.category.color}${pet.category} ${TextColor.DARK_GRAY}• ${pet.rarity.color}${pet.rarity}"
        }

        UIText(subtitleText).constrain {
            x = 0.pixels
            y = SiblingConstraint(2f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardUserColor.toConstraint()
        } childOf rightArea

        val priceContainer = UIContainer().constrain {
            x = 0.pixels
            y = SiblingConstraint(5f)
            width = 100.percent
            height = ChildBasedSizeConstraint()
        } childOf rightArea


        UIText("Buy Price: §6${pet.price.toDouble().formatLargeNumber()}").constrain {
            x = 0.pixels
            y = SiblingConstraint(2f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf priceContainer

        UIText("Sell Price: §6${pet.lvl100Cost.toDouble().formatLargeNumber()}").constrain {
            x = 0.pixels
            y = SiblingConstraint(2f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf priceContainer

        UIText("Exp Required: §3${pet.xpNeeded.formatLargeNumber()}").constrain {
            x = 0.pixels
            y = SiblingConstraint(2f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf priceContainer

        UIText("Profit: §6§l${pet.profit.formatLargeNumber()}").constrain {
            x = 0.pixels
            y = SiblingConstraint(5f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf priceContainer

        UIText("Efficiency: §6§l${pet.coinsPerExp.formatSmallNumber()} §ecoins§6/§3xp").constrain {
            x = 0.pixels
            y = SiblingConstraint(2f)
            width = MinConstraint(TextAspectConstraint(), 100.percent)
            height = 7.pixels
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf priceContainer

    }

    private fun Double.formatLargeNumber(): String {
        if (this >= 1_000_000_000) return String.format("%.2fB", this / 1_000_000_000)
        if (this >= 1_000_000) return String.format("%.2fM", this / 1_000_000)
        if (this >= 1_000) return String.format("%.2fK", this / 1_000)
        return String.format("%.0f", this)
    }

    private fun Double.formatSmallNumber(): String {
        return String.format("%.3f", this)
    }
}
