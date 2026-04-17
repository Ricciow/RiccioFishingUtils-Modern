package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.textinput.UIWrappedDecoratedTextInput
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.model.party.Requisite
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerMyPartyChangedEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.utils.gui.isHidden
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.effects.ScissorEffect

class UICreateParty : UIContainer() {
    private val popup: UIPopup = PartyFinderWindow.popup
    private var party: FishingParty = PartyWebSocket.myParty ?: FishingParty.blankParty()

    private lateinit var titleField: UIDecoratedTextInput
    private lateinit var descriptionField: UIWrappedDecoratedTextInput
    private lateinit var islandField: UIDropdown
    private lateinit var levelField: UIDecoratedTextInput
    private lateinit var maxPlayersField: UIDecoratedTextInput

    private lateinit var waterToggle: UIToggleCard
    private lateinit var lavaToggle: UIToggleCard

    private lateinit var killerToggle: UIToggleCard
    private lateinit var endermanToggle: UIToggleCard
    private lateinit var lootingToggle: UIToggleCard
    private lateinit var brainFoodToggle: UIToggleCard

    private lateinit var submitButton: UIButton

    init {
        registerErrorMessageEvent { message, origin ->
            if (!this.isHidden() && (origin == "/app/party/publish" || origin == "/app/party/edit")) {
                popup.show(message)
            }
        }

        registerMyPartyChangedEvent { updatedParty ->
            if (updatedParty != null) {
                party = updatedParty
                updateFields()
            } else {
                party = FishingParty.blankParty()
                updateFields()
            }
        }

        create()
    }

    private fun create() {
        val border = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = UIScheme.pfSmallSpacing.pixels
            width = 100.percent()
            height = 100.percent() - UIScheme.pfSmallSpacing.pixels
            color = UIScheme.pfCardBorder.toConstraint()
        } childOf this

        val innerContainer = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - (UIScheme.pfCardBorderWidth * 2).pixels()
            height = 100.percent() - (UIScheme.pfCardBorderWidth * 2).pixels()
            color = UIScheme.pfCardBg.toConstraint()
        } childOf border effect ScissorEffect()

        val formContainer = ScrollComponent().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 96.percent()
            height = 96.percent()
        } childOf innerContainer

        createRow1(formContainer)
        createRow2(formContainer)
        createRow3(formContainer)
        createLiquidSection(formContainer)
        createRequirementsSection(formContainer)
        createSubmitButton(formContainer)

        updateButtonLabel()
        setupInteractions()

        updateLiquidVisibility(party.island)
    }

    private fun createRow1(parent: UIContainer) {
        val row1 = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(5f)
            width = 100.percent()
            height = BoundingBoxConstraint()
        } childOf parent

        val titleGroup = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 65.percent() - 2.pixels()
            height = ChildBasedSizeConstraint()
        } childOf row1

        createLabel("TITLE", titleGroup, 0f)
        titleField = UIDecoratedTextInput("Party Title", 2f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf titleGroup
        titleField.setText(party.title)

        val islandGroup = UIContainer().constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
            width = 35.percent() - 3.pixels()
            height = ChildBasedSizeConstraint()
        } childOf row1

        createLabel("ISLAND", islandGroup, 0f)
        islandField = UIDropdown(FishingIslands.toDataOptions(), 0, 2f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
            selectedColor = UIScheme.pfDropdownSelected.toConstraint()
        } childOf islandGroup
        islandField.setSelected(party.island.toDataOption())
    }

    private fun createRow2(parent: UIContainer) {
        createLabel("DESCRIPTION", parent, UIScheme.pfSectionSpacing)
        descriptionField = UIWrappedDecoratedTextInput("Party Description", 2f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfDescriptionHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf parent
        descriptionField.setText(party.description)
    }

    private fun createRow3(parent: UIContainer) {
        val row3 = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfSectionSpacing)
            width = 100.percent()
            height = BoundingBoxConstraint()
        } childOf parent

        val levelGroup = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 20.percent()
            height = ChildBasedSizeConstraint()
        } childOf row3

        createLabel("FISHING LVL", levelGroup, 0f)
        levelField = UIDecoratedTextInput("Fishing Level", 2f, true, 3).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf levelGroup
        levelField.setText(party.level.toString())

        val playersGroup = UIContainer().constrain {
            x = SiblingConstraint(10f)
            y = 0.pixels()
            width = 20.percent()
            height = ChildBasedSizeConstraint()
        } childOf row3

        createLabel("MAX PLAYERS", playersGroup, 0f)
        maxPlayersField = UIDecoratedTextInput("Max Players", 2f, true, 2).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf playersGroup
        maxPlayersField.setText(party.players.max.toString())
    }

    private fun createLiquidSection(parent: UIContainer) {
        createLabel("LIQUID", parent, UIScheme.pfSectionSpacing)

        val envContainer = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        } childOf parent

        waterToggle = UIToggleCard(Requisite("water", "Water", true), party.liquid == LiquidTypes.WATER) { selected ->
            if (selected) {
                party.liquid = LiquidTypes.WATER
                lavaToggle.selected = false
            } else if (!lavaToggle.selected) {
                waterToggle.selected = true
            }
        }.constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
        } childOf envContainer

        lavaToggle = UIToggleCard(Requisite("lava", "Lava", true), party.liquid == LiquidTypes.LAVA) { selected ->
            if (selected) {
                party.liquid = LiquidTypes.LAVA
                waterToggle.selected = false
            } else if (!waterToggle.selected) {
                lavaToggle.selected = true
            }
        }.constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
        } childOf envContainer
    }

    private fun createRequirementsSection(parent: UIContainer) {
        createLabel("REQUIREMENTS", parent, UIScheme.pfSectionSpacing)

        val reqContainer = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf parent

        killerToggle = UIToggleCard(Requisite("has_killer", "Has Killer", true), party.getRequisite("has_killer", "Has Killer").value) {
            updatePartyModel()
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        endermanToggle = UIToggleCard(Requisite("enderman_9", "Enderman 9", true), party.getRequisite("enderman_9", "Enderman 9").value) {
            updatePartyModel()
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        lootingToggle = UIToggleCard(Requisite("looting_5", "Looting 5", true), party.getRequisite("looting_5", "Looting 5").value) {
            updatePartyModel()
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        brainFoodToggle = UIToggleCard(Requisite("brain_food", "Brain Food", true), party.getRequisite("brain_food", "Brain Food").value) {
            updatePartyModel()
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer
    }

    private fun createSubmitButton(parent: UIContainer) {
        submitButton = UIButton("Publish Party", 5f) {
            if (!WebSocketClient.isConnected) {
                popup.show("Not connected to RFU Backend!")
                return@UIButton
            }

            if (DevSettings.devMode && DevSettings.isInSkyblock) {
                updatePartyModel()
                PartyWebSocket.submitParty(party)
                return@UIButton
            } else {
                Party.requestPartyInfo {
                    if (Party.inParty && !Party.isLeader) {
                        popup.show("You must be the party leader to do this!")
                    } else {
                        updatePartyModel()
                        PartyWebSocket.submitParty(party)
                    }
                }
            }

        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint(12f)
            width = 40.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfCardBorder.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
        } childOf parent
    }

    private fun createLabel(text: String, parent: UIContainer, topPadding: Float): UIText {
        return UIText(text).constrain {
            x = 0.pixels()
            y = SiblingConstraint(topPadding)
            width = TextAspectConstraint()
            height = ScaledTextConstraint(UIScheme.pfLabelScale)
            color = UIScheme.pfCardUserColor.toConstraint()
        } childOf parent
    }

    private fun setupInteractions() {
        islandField.onSelect = { option ->
            val island = option.value as FishingIslands
            updateLiquidVisibility(island)
        }
    }

    private fun updateLiquidVisibility(island: FishingIslands) {
        val liquids = island.availableLiquids
        val hasWater = liquids.contains(LiquidTypes.WATER)
        val hasLava = liquids.contains(LiquidTypes.LAVA)

        if (hasWater && hasLava) {
            waterToggle.unhide()
            lavaToggle.unhide()
        } else if (hasWater) {
            waterToggle.selected = true
            lavaToggle.selected = false
            party.liquid = LiquidTypes.WATER

            waterToggle.unhide()
            lavaToggle.hide(true)
        } else if (hasLava) {
            waterToggle.selected = false
            lavaToggle.selected = true
            party.liquid = LiquidTypes.LAVA

            waterToggle.hide(true)
            lavaToggle.unhide()
        }
    }

    private fun updatePartyModel() {
        party.title = titleField.getText()
        party.description = descriptionField.getText()
        party.island = islandField.getSelectedItem().value as FishingIslands
        party.liquid = if (waterToggle.selected) LiquidTypes.WATER else LiquidTypes.LAVA
        party.level = levelField.getText().toIntOrNull() ?: 0
        party.players.max = maxPlayersField.getText().toIntOrNull() ?: 6

        party.setRequisite("has_killer", "Has Killer", killerToggle.selected)
        party.setRequisite("enderman_9", "Enderman 9", endermanToggle.selected)
        party.setRequisite("looting_5", "Looting 5", lootingToggle.selected)
        party.setRequisite("brain_food", "Brain Food", brainFoodToggle.selected)
    }

    private fun updateFields() {
        titleField.setText(party.title)
        descriptionField.setText(party.description)
        islandField.setSelected(party.island.toDataOption())

        updateLiquidVisibility(party.island)

        waterToggle.selected = party.liquid == LiquidTypes.WATER
        lavaToggle.selected = party.liquid == LiquidTypes.LAVA

        levelField.setText(party.level.toString())
        maxPlayersField.setText(party.players.max.toString())

        killerToggle.selected = party.getRequisite("has_killer", "Has Killer").value
        endermanToggle.selected = party.getRequisite("enderman_9", "Enderman 9").value
        lootingToggle.selected = party.getRequisite("looting_5", "Looting 5").value
        brainFoodToggle.selected = party.getRequisite("brain_food", "Brain Food").value

        updateButtonLabel()
    }

    private fun updateButtonLabel() {
        submitButton.updateText(if (PartyWebSocket.myParty == null) "Publish Party" else "Update Party")
    }
}