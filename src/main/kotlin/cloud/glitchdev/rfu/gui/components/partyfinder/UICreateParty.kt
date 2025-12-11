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
import gg.essential.universal.UMatrixStack

class UICreateParty(radius: Float) : UIRoundedRectangle(radius) {
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

    private var needUpdating = true

    init {
        create()
        createInteractions()
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
            SeaCreatures.toDataOptions(party.liquid, party.island, party.fishingType),
            5,
            emptySet(),
            5f,
            false
        ).constrain {
            width = 20.percent()
            height = 100.percent()
        } childOf mobsFieldContainer

        mobsField.setOptionsStates(party.seaCreatures.map {it.toDataOption()}, true)

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
            height = FillConstraint() - (descriptionArea.parent.children.size * 4).pixels()
        }
    }

    fun createInteractions() {
        typeField.onSelect = { data ->
            party.fishingType = data.value as PartyTypes
            updateFields()
        }
        islandField.onSelect = { data ->
            val island = data.value as FishingIslands
            party.island = island
            val availableLiquids = island.availableLiquids
            if(!availableLiquids.contains(party.liquid)) {
                party.liquid = availableLiquids.getOrNull(0) ?: LiquidTypes.LAVA
            }
            updateFields()
        }
        liquidField.onChange = { data ->
            party.liquid = data.value as LiquidTypes
            updateFields()
        }
        killerField.onChange = { state ->
            party.setRequisite("has_killer", "Has Killer", state)
            updateFields()
        }
        endermanField.onChange = { state ->
            party.setRequisite("enderman_9", "Enderman 9", state)
            updateFields()
        }
        lootingField.onChange = { state ->
            party.setRequisite("looting_5", "Looting 5", state)
            updateFields()
        }
        brainFoodField.onChange = { state ->
            party.setRequisite("brain_food", "Brain Food", state)
            updateFields()
        }
        mobsField.onSelectionChanged = { options ->
            party.seaCreatures = options.map { it.value as SeaCreatures }
        }
    }

    fun updateFields() {
        titleField.setText(party.title)
        typeField.setSelected(party.fishingType.toDataOption())
        islandField.setSelected(party.island.toDataOption())
        limitField.setText(party.players.max.toString())
        levelField.setText(party.level.toString())
        liquidField.setSelected(party.liquid.toDataOption())
        killerField.state = party.getRequisite("has_killer", "Has Killer").value
        endermanField.state = party.getRequisite("enderman_9", "Enderman 9").value
        lootingField.state = party.getRequisite("looting_5", "Looting 5").value
        brainFoodField.state = party.getRequisite("brain_food", "Brain Food").value
        mobsField.setValues(SeaCreatures.toDataOptions(party.liquid, party.island, party.fishingType))
        mobsField.setOptionsStates(party.seaCreatures.map {it.toDataOption()}, true)
        descriptionField.setText(party.description)
    }

    override fun draw(matrixStack: UMatrixStack) {
        if(needUpdating && this.getWidth() != 0f) {
            updateFields()
            needUpdating = false
        }
        super.draw(matrixStack)
    }

    companion object {
        var party : FishingParty = FishingParty.blankParty()
    }
}