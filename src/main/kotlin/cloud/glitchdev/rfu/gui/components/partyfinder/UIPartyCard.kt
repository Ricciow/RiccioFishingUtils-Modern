package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.addHoverColoring
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class UIPartyCard(val party: FishingParty, radius : Float) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.decreaseOpacity(UIScheme.primaryColorOpaque, 80).toConstraint()
    val hoverColor = UIScheme.secondaryColorOpaque.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val fontSize = 1f

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        this.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        val mainContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 5.pixels()
            height = 100.percent() - 5.pixels()
        } childOf this

        val leftArea = UIContainer().constrain {
            x = SiblingConstraint()
            y = CenterConstraint()
            width = FillConstraint()
            height = 100.percent()
        } childOf mainContainer

        UIText(party.getTitleString()).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = TextAspectConstraint()
            height = ScaledTextConstraint(fontSize)
            color = textColor
        } childOf leftArea

        UIWrappedText(party.description).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint() - 4.pixels()
            color = textColor
        } childOf leftArea

        UIText(party.getCountString()).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = TextAspectConstraint()
            height = ScaledTextConstraint(fontSize)
            color = textColor
        } childOf leftArea

        val rightArea = UIContainer().constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = max(33.percent(), 160.pixels())- 2.pixels()
            height = 100.percent()
        } childOf mainContainer

        UIText("Type: ${party.fishingType}").constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = TextAspectConstraint()
            height = ScaledTextConstraint(fontSize)
            color = textColor
        } childOf rightArea

        val restrictions = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint()
        } childOf rightArea

        for(requisites in party.requisites) {
            UICheckedText(requisites).constrain {
                x = CramSiblingConstraint(8f)
                y = CramSiblingConstraint(2f)
                width = 40.percent()
                height = 9.pixels()
            } childOf restrictions
        }

        UIWrappedText("SCs: ${party.getSeaCreatureString()}").constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = 20.pixels()
        } childOf rightArea
    }
}