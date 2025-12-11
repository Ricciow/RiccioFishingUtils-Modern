package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.checkbox.UIRadio
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.dropdown.UISelectionDropdown
import cloud.glitchdev.rfu.gui.components.textinput.UIWrappedDecoratedTextInput
import cloud.glitchdev.rfu.model.party.FishingParty
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

    var party : FishingParty = FishingParty.blankParty()

    lateinit var titleField : UIDecoratedTextInput
    lateinit var typeField : UIDropdown
    lateinit var islandField : UIDropdown
    lateinit var limitField : UIDecoratedTextInput
    lateinit var levelField : UIDecoratedTextInput
    lateinit var liquidField : UIRadio
    lateinit var killerField : UICheckbox
    lateinit var endermanField : UICheckbox
    lateinit var lootingField: UICheckbox
    lateinit var brainFoodField : UICheckbox
    lateinit var mobsField : UISelectionDropdown
    lateinit var descriptionField : UIWrappedDecoratedTextInput

    init {
        create()
        createInteractions()
        updateMobField()
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

        titleField = UIDecoratedTextInput("Max 20 chars", 5f, false, 20)
        titleArea.addSection(titleField)

        val typeArea = UITitledSection("Type:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        typeField = UIDropdown(PartyTypes.toDataOptions(), 0, 5f)
        typeArea.addSection(typeField)

        val islandArea = UITitledSection("Island:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        islandField = UIDropdown(FishingIslands.toDataOptions(), 0, 5f)
        islandArea.addSection(islandField)

        val limitArea = UITitledSection("Max Players:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        limitField = UIDecoratedTextInput("6", 5f, true, 2)
        limitArea.addSection(limitField)

        val levelArea = UITitledSection("Min Level:").constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 20.percent() - 1.6.pixels()
            height = 100.percent()
        } childOf fieldArea

        levelField = UIDecoratedTextInput("0", 5f, true, 3)
        levelArea.addSection(levelField)

        val liquidArea = UITitledSection("Liquid:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        liquidField = UIRadio(LiquidTypes.toDataOptions(), 0)
        liquidArea.addSection(liquidField)

        val requisitesArea = UITitledSection("Extras:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 25.pixels()
        } childOf container

        val requisitesFields = UIContainer()
        requisitesArea.addSection(requisitesFields)

        killerField = UICheckbox("Has Killer").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        endermanField = UICheckbox("Enderman 9").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        lootingField = UICheckbox("Looting 5").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf requisitesFields

        brainFoodField = UICheckbox("Brain Food").constrain {
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

        val mobsFieldContainer = UIContainer()

        mobsField = UISelectionDropdown(
            arrayListOf(),
            5,
            emptySet(),
            5f,
            false,
            "Max 5"
        ).constrain {
            width = 20.percent()
            height = 100.percent()
        } childOf mobsFieldContainer

        mobsArea.addSection(mobsFieldContainer)

        val descriptionArea = UITitledSection("Description:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
        } childOf container

        descriptionField = UIWrappedDecoratedTextInput("Max 200 Chars", 5f, 200)

        descriptionArea.addSection(descriptionField)

        val endArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 20.pixels()
        } childOf container

        UIButton("Create", 5f).constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = 10.percent()
            height = 100.percent()
        } childOf endArea

        descriptionArea.constrain {
            //Remove space from siblingConstraint paddings
            height = FillConstraint() - ((descriptionArea.parent.children.size) * 4).pixels()
        }
    }

    fun createInteractions() {
        typeField.onSelect = { data ->
            println("Type")
            party.fishingType = data.value as PartyTypes
            updateMobField()
        }
        islandField.onSelect = { data ->
            println("Island")
            party.island = data.value as FishingIslands
            updateMobField()
        }
        liquidField.onChange = { data ->
            println("Liquid")
            party.liquid = data.value as LiquidTypes
            updateMobField()
        }
    }

    fun updateMobField() {
        println("update")
        mobsField.setValues(SeaCreatures.toDataOptions(party.liquid, party.island, party.fishingType))
    }
}