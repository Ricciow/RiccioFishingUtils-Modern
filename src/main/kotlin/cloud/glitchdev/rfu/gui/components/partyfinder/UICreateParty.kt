package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.skyblock.Skills
import cloud.glitchdev.rfu.data.party.PartyPresetsManager
import cloud.glitchdev.rfu.data.other.data.PartyPresetData
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.textinput.UIWrappedDecoratedTextInput
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.model.party.PartyTypeOption
import cloud.glitchdev.rfu.party.PartyRequirementsManager
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerMyPartyChangedEvent
import cloud.glitchdev.rfu.utils.Coroutines
import kotlinx.coroutines.delay
import kotlin.jvm.optionals.getOrNull
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
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.effects.ScissorEffect

class UICreateParty : UIContainer() {
    private val popup: UIPopup get() = PartyFinderWindow.popup
    private var party: FishingParty = PartyWebSocket.myParty ?: loadInitialParty()

    private fun loadInitialParty(): FishingParty {
        val blank = FishingParty.blankParty()
        val entry = PartyPresetsManager.getEntry()
        entry.lastPartyState?.applyTo(blank)
        return blank
    }

    fun saveSessionState() {
        if (!::titleField.isInitialized) return
        updatePartyModel()
        val entry = PartyPresetsManager.getEntry()
        val state = entry.lastPartyState ?: PartyPresetData()
        state.copyFrom(party, "last_session")
        entry.lastPartyState = state
        PartyPresetsManager.saveEntry(entry)
    }

    private lateinit var titleField: UIDecoratedTextInput
    private lateinit var descriptionField: UIWrappedDecoratedTextInput
    private lateinit var islandField: UIDropdown
    private lateinit var levelField: UIDecoratedTextInput
    private lateinit var overflowField: UIDecoratedTextInput
    private lateinit var maxPlayersField: UIDecoratedTextInput
    private val partyTypeToggles = linkedMapOf<PartyTypeOption, UIToggleCard>()
    private lateinit var killerToggle: UIToggleCard
    private lateinit var endermanToggle: UIToggleCard
    private lateinit var lootingToggle: UIToggleCard
    private lateinit var brainFoodToggle: UIToggleCard
    private lateinit var bloodshotToggle: UIToggleCard
    private lateinit var submitButton: UIButton
    private var lastSubmitTime = 0L

    init {
        registerErrorMessageEvent { message, origin ->
            if (!this.isHidden() && (origin == "/app/party/publish" || origin == "/app/party/edit")) {
                popup.show(message)
                submitButton.disabled = false
            }
        }

        registerMyPartyChangedEvent { updatedParty ->
            if (updatedParty != null) {
                party = updatedParty
                updateFields()
            } else {
                updateButtonLabel()
            }
        }

        registerLocationEvent { event ->
            if (PartyWebSocket.myParty != null) return@registerLocationEvent
            val islandName = event.map.getOrNull() ?: return@registerLocationEvent
            val newIsland = FishingIslands.findIslandObject(islandName)
            if (newIsland != null && newIsland != FishingIslands.OTHER && newIsland != FishingIslands.NOT_SB && newIsland != FishingIslands.UNKNOWN) {
                if (party.island != newIsland) {
                    party.island = newIsland
                    islandField.setSelected(newIsland.toDataOption())
                    updatePartyTypeVisibility(newIsland)
                }
            }
        }

        create()
    }

    private fun create() {
        val border = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = UIScheme.pfSmallSpacing.pixels
            width = 100.percent()
            height = max(0.pixels, 100.percent() - UIScheme.pfSmallSpacing.pixels)
            color = UIScheme.pfCardBorder.toConstraint()
        } childOf this

        val innerContainer = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(0.pixels, 100.percent() - (UIScheme.pfCardBorderWidth * 2).pixels())
            height = max(0.pixels, 100.percent() - (UIScheme.pfCardBorderWidth * 2).pixels())
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
        createPartyTypeSection(formContainer)
        createRequirementsSection(formContainer)
        createSubmitButton(formContainer)

        updateButtonLabel()
        setupInteractions()

        updatePartyTypeVisibility(party.island)
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
            optionColor = UIScheme.pfDropdownOptionBg.toConstraint()
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
        levelField.setText(party.level.coerceIn(0, 50).toString())

        val overflowGroup = UIContainer().constrain {
            x = SiblingConstraint(10f)
            y = 0.pixels()
            width = 20.percent()
            height = ChildBasedSizeConstraint()
        } childOf row3

        createLabel("OVERFLOW", overflowGroup, 0f)
        overflowField = UIDecoratedTextInput("e.g. 1.5b, 500m", 2f, maxChars = 32).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfLabelSpacing)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf overflowGroup
        overflowField.setText(party.requiredOverflow().toString())
        overflowField.isEnabled = party.level >= 50

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

    private fun createPartyTypeSection(parent: UIContainer) {
        createLabel("PARTY TYPE", parent, UIScheme.pfSectionSpacing)

        val envContainer = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        } childOf parent

        val currentType = PartyTypeOption.from(party.fishingType, party.liquid)
        for (type in PartyTypeOption.entries) {
            partyTypeToggles[type] = UIToggleCard(
                DataOption(type.name.lowercase(), type.label), currentType == type
            ) { selectPartyType(type) }.constrain {
                x = SiblingConstraint(2f)
                y = CenterConstraint()
            } childOf envContainer
        }
    }

    private fun createRequirementsSection(parent: UIContainer) {
        createLabel("REQUIREMENTS", parent, UIScheme.pfSectionSpacing)

        val reqContainer = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf parent

        killerToggle = UIToggleCard(party.getRequisite("has_killer", "Has Killer").toDataOption(), party.getRequisite("has_killer", "Has Killer").value) {
            updatePartyModel()
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        endermanToggle = UIToggleCard(party.getRequisite("enderman_9", "Enderman 9").toDataOption(), party.getRequisite("enderman_9", "Enderman 9").value) { selected ->
            if (selected && !PartyRequirementsManager.hasEnderman9()) {
                endermanToggle.selected = false
                val result = PartyRequirementsManager.PartyValidationResult.MissingRequisite("enderman_9", "Enderman 9")
                popup.show(result.getErrorMessage())
            } else {
                updatePartyModel()
            }
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        lootingToggle = UIToggleCard(party.getRequisite("looting_5", "Looting 5").toDataOption(), party.getRequisite("looting_5", "Looting 5").value) { selected ->
            if (selected && !PartyRequirementsManager.hasLooting5()) {
                lootingToggle.selected = false
                val result = PartyRequirementsManager.PartyValidationResult.MissingRequisite("looting_5", "Looting 5")
                popup.show(result.getErrorMessage())
            } else {
                updatePartyModel()
            }
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer


        brainFoodToggle = UIToggleCard(party.getRequisite("brain_food", "Brain Food").toDataOption(), party.getRequisite("brain_food", "Brain Food").value) { selected ->
            if (selected && !PartyRequirementsManager.hasBrainFood()) {
                brainFoodToggle.selected = false
                val result = PartyRequirementsManager.PartyValidationResult.MissingBrainFood
                popup.show(result.getErrorMessage())
            } else {
                updatePartyModel()
            }
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer

        bloodshotToggle = UIToggleCard(party.getRequisite("bloodshot", "Bloodshot").toDataOption(), party.getRequisite("bloodshot", "Bloodshot").value) { selected ->
            if (selected && !PartyRequirementsManager.hasBloodshotBelt()) {
                bloodshotToggle.selected = false
                val result = PartyRequirementsManager.PartyValidationResult.MissingBloodshot
                popup.show(result.getErrorMessage())
            } else {
                updatePartyModel()
            }
        }.constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf reqContainer
    }

    private fun createSubmitButton(parent: UIContainer) {
        val buttonWrapper = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(12f)
            width = 50.percent()
            height = UIScheme.pfInputHeight.pixels()
        } childOf parent

        UIButton("Presets", 5f) {
            saveSessionState()
            PartyFinderWindow.presetsModal.show(party) { selectedPreset ->
                selectedPreset.applyTo(party)
                updateFields()
                saveSessionState()
            }
        }.constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 28.percent()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfCardBorder.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
        } childOf buttonWrapper

        submitButton = UIButton("Publish Party", 5f) {
            val now = System.currentTimeMillis()
            val elapsed = now - lastSubmitTime
            if (elapsed < 5000L) {
                val secondsLeft = ((5000L - elapsed) / 1000L) + 1
                popup.show("Please wait ${secondsLeft}s before trying again!")
                return@UIButton
            }

            if (!WebSocketClient.isConnected) {
                popup.show("Not connected to RFU Backend!")
                return@UIButton
            }

            if (overflowField.isEnabled && parseOverflow() == null) {
                popup.show("Enter a valid overflow XP amount, such as 1.5b or 500m.")
                return@UIButton
            }

            updatePartyModel()
            saveSessionState()
            val validation = PartyRequirementsManager.canCreateParty(party)
            if (!validation.isSuccess) {
                popup.show(validation.getErrorMessage())
                return@UIButton
            }

            lastSubmitTime = now
            submitButton.disabled = true

            Coroutines.launch {
                delay(5000)
                submitButton.disabled = false
            }

            if (DevSettings.devMode && DevSettings.isInSkyblock) {
                PartyWebSocket.submitParty(party)
            } else {
                Party.requestPartyInfo {
                    if (Party.inParty && !Party.isLeader) {
                        popup.show("You must be the party leader to do this!")
                        submitButton.disabled = false
                    } else {
                        PartyWebSocket.submitParty(party)
                    }
                }
            }

        }.constrain {
            x = SiblingConstraint(6f)
            y = CenterConstraint()
            width = 100.percent() - 28.percent() - 6.pixels()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfCardBorder.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
        } childOf buttonWrapper
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
        levelField.onChange = { text ->
            val level = (text.toIntOrNull() ?: 0).coerceIn(0, 50)
            if (text.toIntOrNull() != null && text.toInt() != level) levelField.setText(level.toString())
            overflowField.isEnabled = level == 50
        }
        islandField.onSelect = { option ->
            val island = option.value as FishingIslands
            party.island = island
            updatePartyTypeVisibility(island)
        }
    }

    private fun updatePartyTypeVisibility(island: FishingIslands) {
        for ((type, toggle) in partyTypeToggles) {
            toggle.hide(true)
            if (type.liquidFor(island) != null) toggle.unhide(useLastPosition = false)
        }
        val currentType = PartyTypeOption.from(party.fishingType, party.liquid)
        selectPartyType(currentType.takeIf { it.liquidFor(island) != null }
            ?: PartyTypeOption.entries.first { it.liquidFor(island) != null })
    }

    private fun selectPartyType(type: PartyTypeOption) {
        val liquid = type.liquidFor(party.island) ?: return
        party.fishingType = type.fishingType
        party.liquid = liquid
        for ((option, toggle) in partyTypeToggles) {
            toggle.selected = option == type
        }
    }

    private fun updatePartyModel() {
        party.profileId = User.profileId
        party.title = titleField.getText()
        party.description = descriptionField.getText()
        party.island = islandField.getSelectedItem().value as FishingIslands
        updatePartyTypeVisibility(party.island)
        party.level = (levelField.getText().toIntOrNull() ?: 0).coerceIn(0, 50)
        party.overflow = if (party.level == 50) parseOverflow() ?: party.overflow else 0L
        party.players.current = if (Party.inParty) Party.memberCount else 1
        party.players.max = maxPlayersField.getText().toIntOrNull() ?: 6

        party.setRequisite("has_killer", "Has Killer", killerToggle.selected)
        party.setRequisite("enderman_9", "Enderman 9", endermanToggle.selected)
        party.setRequisite("looting_5", "Looting 5", lootingToggle.selected)
        party.setRequisite("brain_food", "Brain Food", brainFoodToggle.selected)
        party.setRequisite("bloodshot", "Bloodshot", bloodshotToggle.selected)
    }

    private fun updateFields() {
        titleField.setText(party.title)
        descriptionField.setText(party.description)
        islandField.setSelected(party.island.toDataOption())

        updatePartyTypeVisibility(party.island)

        levelField.setText(party.level.coerceIn(0, 50).toString())
        overflowField.setText(party.requiredOverflow().toString())
        overflowField.isEnabled = party.level >= 50
        maxPlayersField.setText(party.players.max.toString())

        killerToggle.selected = party.getRequisite("has_killer", "Has Killer").value
        endermanToggle.selected = party.getRequisite("enderman_9", "Enderman 9").value
        lootingToggle.selected = party.getRequisite("looting_5", "Looting 5").value
        brainFoodToggle.selected = party.getRequisite("brain_food", "Brain Food").value
        bloodshotToggle.selected = party.getRequisite("bloodshot", "Bloodshot").value

        updateButtonLabel()
    }

    private fun updateButtonLabel() {
        submitButton.updateText(if (PartyWebSocket.myParty == null) "Publish Party" else "Update Party")
        submitButton.disabled = false
    }

    private fun parseOverflow(): Long? {
        val text = overflowField.getText()
        if (text.isBlank()) return 0L
        return Skills.parseXpOrNull(text)?.takeIf { it <= Long.MAX_VALUE - Skills.getTotalXpAtLevel(50) }
    }
}
