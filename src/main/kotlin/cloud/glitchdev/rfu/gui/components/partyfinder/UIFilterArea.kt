package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.CramAwareMaxSizeConstraint
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.model.party.FishingParty
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.YConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.div
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint

class UIFilterArea(private val filterHeight : YConstraint, var onFilterChange: () -> Unit = {}) : UIContainer() {
    lateinit var searchField: UIDecoratedTextInput
    lateinit var levelField: UIDecoratedTextInput
    lateinit var canJoinToggle: UIToggleCard
    lateinit var waterToggle: UIToggleCard
    lateinit var lavaToggle: UIToggleCard
    lateinit var killerToggle: UIToggleCard
    lateinit var endermanToggle: UIToggleCard
    lateinit var lootingToggle: UIToggleCard
    lateinit var brainFoodToggle: UIToggleCard

    init {
        create()
        createInteractions()
    }

    fun create() {
        val topArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = filterHeight / 3
        } childOf this

        searchField = UIDecoratedTextInput("Search", 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 90.percent() - 2.pixels()
            height = 80.percent()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf topArea

        levelField = UIDecoratedTextInput("LVL", 2f, true, 3).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 10.percent()
            height = 80.percent()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf topArea

        val bottomArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = CramAwareMaxSizeConstraint()
        } childOf this

        canJoinToggle = UIToggleCard(DataOption("can_join", "Can Join")) {
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        waterToggle = UIToggleCard(DataOption("water", "Water"), false) { selected ->
            if (selected) {
                lavaToggle.selected = false
            }
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        lavaToggle = UIToggleCard(DataOption("lava", "Lava"), false) { selected ->
            if (selected) {
                waterToggle.selected = false
            }
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        killerToggle = UIToggleCard(DataOption("has_killer", "Has Killer")) {
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        endermanToggle = UIToggleCard(DataOption("enderman_9", "Enderman 9")) {
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        lootingToggle = UIToggleCard(DataOption("looting_5", "Looting 5")) {
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea

        brainFoodToggle = UIToggleCard(DataOption("brain_food", "Brain Food")) {
            onFilterChange()
        }.constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        } childOf bottomArea
    }

    fun createInteractions() {
        searchField.onChange = {
            onFilterChange()
        }
        levelField.onChange = {
            onFilterChange()
        }
    }

    fun applyFilter(parties: List<FishingParty>): MutableList<FishingParty> {
        return parties.filter { party ->
            val text = searchField.getText().lowercase()
            if (!(
                text.isEmpty() ||
                party.island.island.lowercase().contains(text) ||
                party.title.lowercase().contains(text) ||
                party.description.lowercase().contains(text)
            )) return@filter false
            if (levelField.getText().isNotEmpty() && party.level < levelField.getText().toInt()) return@filter false

            if (waterToggle.selected && party.liquid != LiquidTypes.WATER) return@filter false
            if (lavaToggle.selected && party.liquid != LiquidTypes.LAVA) return@filter false

            if (canJoinToggle.selected && party.players.current >= party.players.max) return@filter false

            if (killerToggle.selected && !party.getRequisite("has_killer", "Has Killer").value) return@filter false
            if (endermanToggle.selected && !party.getRequisite("enderman_9", "Enderman 9").value) return@filter false
            if (lootingToggle.selected && !party.getRequisite("looting_5", "Looting 5").value) return@filter false
            if (brainFoodToggle.selected && !party.getRequisite("brain_food", "Brain Food").value) return@filter false

            return@filter true
        } as MutableList<FishingParty>
    }
}