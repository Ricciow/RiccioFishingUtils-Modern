package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.checkbox.UIRadio
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.dropdown.UISelectionDropdown
import cloud.glitchdev.rfu.gui.components.textinput.UIWrappedDecoratedTextInput
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels

class UICreateParty(radius: Float) : UIRoundedRectangle(radius) {
    init {
        create()
    }

    fun create() {
        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 96.percent()
            height = 100.percent()
        } childOf this

        UIWrappedText("Create your party here, parties queued will last for at most 30 minutes.").constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 100.percent()
            height = 18.pixels()
        } childOf container

        val fieldArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        val titleArea = UITitledSection("Title:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        val titleField = UIDecoratedTextInput("Max 20 chars", 5f, false, 20)
        titleArea.addSection(titleField)

        val typeArea = UITitledSection("Type:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        val typeField = UIDropdown(PartyTypes.toDataOptions(), 0, 5f)
        typeArea.addSection(typeField)

        val islandArea = UITitledSection("Island:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        val islandField = UIDropdown(FishingIslands.toDataOptions(), 0, 5f)
        islandArea.addSection(islandField)

        val limitArea = UITitledSection("Max Players:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        val limitField = UIDecoratedTextInput("6", 5f, true, 2)
        limitArea.addSection(limitField)

        val levelArea = UITitledSection("Min Level:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        val levelField = UIDecoratedTextInput("0", 5f, true, 3)
        levelArea.addSection(levelField)

        val liquidArea = UITitledSection("Liquid:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        val liquidField = UIRadio(LiquidTypes.toDataOptions(), 0)
        liquidArea.addSection(liquidField)

        val requisitesArea = UITitledSection("Extras:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        val requisitesFields = UIContainer()
        requisitesArea.addSection(requisitesFields)

        UICheckbox("Has Killer").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        UICheckbox("Enderman 9").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        UICheckbox("Looting 5").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        UICheckbox("Brain Food").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        val mobsArea = UITitledSection("Sea Creatures:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        val mobsField = UIContainer()

        UISelectionDropdown(
            SeaCreatures.toDataOptions(LiquidTypes.LAVA, FishingIslands.ISLE),
            5,
            emptySet(),
            5f,
            false,
            "Max 5"
        ).constrain {
            width = 20.percent()
            height = 100.percent()
        } childOf mobsField

        mobsArea.addSection(mobsField)

        val descriptionArea = UITitledSection("Description:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
        } childOf container

        descriptionArea.constrain {
            //Remove space from siblingConstraint paddings
            height = FillConstraint() - ((descriptionArea.parent.children.size) * 4).pixels()
        }

        val descriptionField = UIWrappedDecoratedTextInput("Max 200 Chars", 5f, 200)

        descriptionArea.addSection(descriptionField)
    }
}