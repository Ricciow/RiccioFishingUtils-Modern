package cloud.glitchdev.rfu.gui.components.pets

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.elementa.JustifiedCramSiblingConstraint
import cloud.glitchdev.rfu.gui.components.partyfinder.UIToggleCard
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.model.pets.ItemRarity
import cloud.glitchdev.rfu.model.pets.PetCategory
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import java.util.Timer
import java.util.TimerTask

class UIPetFilterArea(var onSearch: () -> Unit = {}) : UIContainer() {
    lateinit var categoryDropdown: UIDropdown
    lateinit var rarityDropdown: UIDropdown
    lateinit var countField: UIDecoratedTextInput
    lateinit var maxLevelField: UIDecoratedTextInput
    lateinit var candyToggle: UIToggleCard
    lateinit var uniqueToggle: UIToggleCard

    private var debounceTimer: Timer? = null

    init {
        create()
    }

    private fun triggerUpdate() {
        countField.isEnabled = !uniqueToggle.selected
        debounceTimer?.cancel()
        debounceTimer = Timer()
        debounceTimer?.schedule(object : TimerTask() {
            override fun run() {
                onSearch()
            }
        }, 500)
    }

    private fun create() {
        val topArea = UIContainer().constrain {
            x = CenterConstraint()
            y = 2.pixels
            width = 96.percent
            height = 18.pixels
        } childOf this

        val categories = arrayListOf(DataOption("ALL", "All Categories"))
        PetCategory.entries.forEach { categories.add(DataOption(it, it.toString())) }
        val defaultCategoryIndex = categories.indexOfFirst { it.value == PetCategory.FISHING }.coerceAtLeast(0)

        categoryDropdown = UIDropdown(categories, defaultCategoryIndex, 2f, label = "Category").constrain {
            x = JustifiedCramSiblingConstraint(2f)
            y = CenterConstraint()
            width = 35.percent()
            height = 100.percent()
        }.apply {
            onSelect = { triggerUpdate() }
        } childOf topArea

        val rarities = arrayListOf(DataOption("ALL", "All Rarities"))
        ItemRarity.entries
            .takeWhile { it <= ItemRarity.MYTHIC }
            .forEach { rarities.add(DataOption(it, it.toString())) }

        val defaultRarityIndex = rarities.indexOfFirst { it.value == ItemRarity.LEGENDARY }.coerceAtLeast(0)

        rarityDropdown = UIDropdown(rarities, defaultRarityIndex, 2f, label = "Rarity").constrain {
            x = JustifiedCramSiblingConstraint(2f)
            y = CenterConstraint()
            width = 35.percent()
            height = 100.percent()
        }.apply {
            onSelect = { triggerUpdate() }
        } childOf topArea

        maxLevelField = UIDecoratedTextInput("Max Lvl", 2f, true, 3).constrain {
            x = JustifiedCramSiblingConstraint(2f)
            y = CenterConstraint()
            width = 14.percent
            height = 100.percent
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        }.apply {
            onChange = { triggerUpdate() }
        } childOf topArea

        countField = UIDecoratedTextInput("Count", 2f, true, 2).constrain {
            x = JustifiedCramSiblingConstraint(2f)
            y = CenterConstraint()
            width = 14.percent
            height = 100.percent
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        }.apply {
            onChange = { triggerUpdate() }
        } childOf topArea
        countField.setText("12")


        val bottomArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 96.percent
            height = ChildBasedMaxSizeConstraint()
        } childOf this

        candyToggle = UIToggleCard(DataOption("candy", "Filter Candy"), true).constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        }.apply {
            onToggle = { triggerUpdate() }
        } childOf bottomArea

        uniqueToggle = UIToggleCard(DataOption("unique", "Unique Pets"), false).constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
        }.apply {
            onToggle = { triggerUpdate() }
        } childOf bottomArea
    }

    fun getCategory(): PetCategory? = categoryDropdown.getSelectedItem().value as? PetCategory
    fun getRarity(): ItemRarity? = rarityDropdown.getSelectedItem().value as? ItemRarity
    fun getCount(): Int = countField.getText().toIntOrNull()?.coerceIn(1, 24) ?: 12
    fun getMaxLevel(): Int? = maxLevelField.getText().toIntOrNull()
    fun getFilterCandy(): Boolean = candyToggle.selected
    fun getUnique(): Boolean = uniqueToggle.selected
}
