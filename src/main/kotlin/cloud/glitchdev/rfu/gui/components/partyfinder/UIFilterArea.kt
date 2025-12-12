package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.checkbox.UIRadio
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.model.party.FishingParty
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels

class UIFilterArea(radius: Float, var onFilterChange: () -> Unit = {}) : UIRoundedRectangle(radius) {
    lateinit var islandField: UIDropdown
    lateinit var typeField: UIDropdown
    lateinit var seaCreatureField: UIDecoratedTextInput
    lateinit var levelField: UIDecoratedTextInput
    lateinit var canJoinField: UICheckbox
    lateinit var liquidField: UIRadio
    lateinit var killerField: UICheckbox
    lateinit var endermanField: UICheckbox
    lateinit var lootingField: UICheckbox
    lateinit var brainFoodField: UICheckbox

    init {
        create()
        createInteractions()
    }

    fun create() {
        val topArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = 50.percent()
        } childOf this

        islandField = UIDropdown(FishingIslands.toDataOptions(), 0, 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 25.percent()
            height = 50.percent()
        } childOf topArea

        typeField = UIDropdown(PartyTypes.toDataOptions(), 0, 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 15.percent()
            height = 50.percent()
        } childOf topArea

        seaCreatureField = UIDecoratedTextInput("Sea Creature", 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = FillConstraint() - 6.pixels()
            height = 50.percent()
        } childOf topArea

        levelField = UIDecoratedTextInput("LVL", 2f, true, 3).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 10.percent()
            height = 50.percent()
        } childOf topArea

        val bottomArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = 50.percent()
        } childOf this

        canJoinField = UICheckbox("Can Join").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        liquidField = UIRadio(LiquidTypes.toDataOptions(), 0).constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = 80.pixels()
            height = 50.percent()
        } childOf bottomArea

        killerField = UICheckbox("Has Killer").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        endermanField = UICheckbox("Enderman 9").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        lootingField = UICheckbox("Looting 5").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        brainFoodField = UICheckbox("Brain Food").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea
    }

    fun createInteractions() {
        islandField.onSelect = {
            onFilterChange()
        }
        typeField.onSelect = {
            onFilterChange()
        }
        seaCreatureField.onChange = {
            onFilterChange()
        }
        levelField.onChange = {
            onFilterChange()
        }
        liquidField.onChange = {
            onFilterChange()
        }
        killerField.onChange = {
            onFilterChange()
        }
        endermanField.onChange = {
            onFilterChange()
        }
        lootingField.onChange = {
            onFilterChange()
        }
        brainFoodField.onChange = {
            onFilterChange()
        }
    }

    fun applyFilter(parties: MutableList<FishingParty>): MutableList<FishingParty> {
        return parties.filter { party ->
            if (party.island != islandField.getSelectedItem().value) return@filter false
            if (party.fishingType != typeField.getSelectedItem().value) return@filter false
            if (!party.seaCreatures.joinToString(" ") { it.scName.lowercase() }
                    .contains(seaCreatureField.getText().lowercase().toRegex())) return@filter false

            if (!levelField.getText().isEmpty() && party.level <= levelField.getText().toInt()) return@filter false
            if (party.liquid != liquidField.getSelectedValue().value) return@filter false
            if (killerField.state && !party.getRequisite("has_killer", "Has Killer").value) return@filter false
            if (endermanField.state && !party.getRequisite("enderman_9", "Enderman 9").value) return@filter false
            if (lootingField.state && !party.getRequisite("looting_5", "Looting 5").value) return@filter false
            if (brainFoodField.state && !party.getRequisite("brain_food", "Brain Food").value) return@filter false

            return@filter true
        } as MutableList<FishingParty>
    }
}