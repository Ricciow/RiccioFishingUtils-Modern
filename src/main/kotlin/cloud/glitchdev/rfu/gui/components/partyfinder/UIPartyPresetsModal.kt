package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.PartyPresetData
import cloud.glitchdev.rfu.data.other.data.PartyPresetsEntry
import cloud.glitchdev.rfu.feature.other.EmojiFeature
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.UIScheme.increaseOpacity
import cloud.glitchdev.rfu.gui.components.Colorable
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.model.party.FishingParty
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.effects.ScissorEffect
import java.awt.Color

class UIPartyPresetsModal(
    val radiusPopup: Float = 5f
) : UIBlock(), Colorable {
    var backgroundColor = Color.BLACK.increaseOpacity(127).toConstraint()
    var primaryColor = UIScheme.pfCardBorder.toConstraint()
    var innerColor = UIScheme.pfCardBg.toConstraint()
    var borderWidth = 1.5f

    private var currentParty: FishingParty? = null
    private var onLoadPreset: ((PartyPresetData) -> Unit)? = null

    private lateinit var modalContainer: UIRoundedRectangle
    private lateinit var innerBg: UIRoundedRectangle
    private lateinit var presetNameInput: UIDecoratedTextInput
    private lateinit var saveButton: UIButton
    private lateinit var scrollComponent: ScrollComponent
    private lateinit var listContainer: UIContainer
    private lateinit var emptyNotice: UIWrappedText

    companion object {
        const val PRESETS_KEY = "party_finder_presets"

        fun getPresetsEntry(): PartyPresetsEntry {
            return (OtherManager.getField(PRESETS_KEY) { PartyPresetsEntry() } as? PartyPresetsEntry)
                ?: PartyPresetsEntry()
        }

        fun savePresetsEntry(entry: PartyPresetsEntry) {
            OtherManager.setField(PRESETS_KEY, entry)
            OtherManager.file.save()
        }
    }

    init {
        create()
    }

    fun show(currentParty: FishingParty, onLoad: (PartyPresetData) -> Unit) {
        this.currentParty = currentParty
        this.onLoadPreset = onLoad
        presetNameInput.setText(currentParty.title.trim())
        refreshPresetsList()
        unhide()
    }

    fun hideModal() {
        this.hide(true)
    }

    private fun create() {
        this.constrain {
            x = RelativeWindowConstraint(0f)
            y = RelativeWindowConstraint(0f)
            width = RelativeWindowConstraint(1f)
            height = RelativeWindowConstraint(1f)
            color = backgroundColor
            isFloating = true
        }

        this.onMouseClick {
            it.stopPropagation()
        }

        modalContainer = UIRoundedRectangle(radiusPopup).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 45.percent()
            height = 65.percent()
            color = primaryColor
        } childOf this

        innerBg = UIRoundedRectangle(radiusPopup).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - (borderWidth * 2).pixels()
            height = 100.percent() - (borderWidth * 2).pixels()
            color = innerColor
        } childOf modalContainer effect ScissorEffect()

        val content = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 94.percent()
            height = 94.percent()
        } childOf innerBg

        createHeader(content)
        createSaveSection(content)
        createSeparator(content)
        createListSection(content)
    }

    private fun createHeader(parent: UIContainer) {
        val header = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 20.pixels()
        } childOf parent

        UIText("Party Presets").constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.3f)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        UIButton("X", 4f) {
            hideModal()
        }.constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = AspectConstraint(1f)
            height = 16.pixels()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfCardOverlayHoverColor.toConstraint()
            textColor = UIScheme.primaryTextColor.toConstraint()
            hoverTextColor = UIScheme.primaryTextColor.toConstraint()
        } childOf header
    }

    private fun createSaveSection(parent: UIContainer) {
        val saveSection = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(8f)
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf parent

        createLabel("PRESET NAME", saveSection, 0f)

        val row = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = 100.percent()
            height = UIScheme.pfInputHeight.pixels()
        } childOf saveSection

        presetNameInput = UIDecoratedTextInput("Preset Name", 2f).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 68.percent()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf row

        saveButton = UIButton("Save", 5f) {
            val name = presetNameInput.getText().trim()
            if (name.isEmpty()) {
                PartyFinderWindow.popup.show("Please enter a preset name!")
                return@UIButton
            }
            val party = currentParty ?: return@UIButton
            val entry = getPresetsEntry()
            val preset = entry.presets.getOrPut(name) { PartyPresetData() }
            preset.copyFrom(party, name)
            savePresetsEntry(entry)

            presetNameInput.setText(party.title.trim())
            refreshPresetsList()
        }.constrain {
            x = SiblingConstraint(6f)
            y = CenterConstraint()
            width = 100.percent() - 68.percent() - 6.pixels()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfCardBorder.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
        } childOf row
    }

    private fun createSeparator(parent: UIContainer) {
        UIRoundedRectangle(1f).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(8f)
            width = 100.percent()
            height = 1.pixels()
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf parent
    }

    private fun createListSection(parent: UIContainer) {
        val listSection = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(6f)
            width = 100.percent()
            height = FillConstraint()
        } childOf parent

        createLabel("SAVED PRESETS", listSection, 0f)

        listContainer = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(3f)
            width = 100.percent()
            height = FillConstraint()
        } childOf listSection

        scrollComponent = ScrollComponent().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent() - 4.pixels()
            height = 100.percent()
        } childOf listContainer

        val scrollBar = UIRoundedRectangle(3f).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            height = 100.percent()
            color = UIScheme.pfScrollBar.toConstraint()
        } childOf listContainer

        scrollComponent.setScrollBarComponent(scrollBar, hideWhenUseless = true, isHorizontal = false)

        emptyNotice = UIWrappedText("No presets saved yet.\nSave a party!", centered = true)
            .constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = TextWrappingConstraint()
            color = UIScheme.placeholderTextColor.toConstraint()
        } childOf listContainer
    }

    fun refreshPresetsList() {
        if (!::scrollComponent.isInitialized) return

        scrollComponent.clearChildren()
        val entry = getPresetsEntry()
        val presets = entry.presets.values.toList()

        if (presets.isEmpty()) {
            emptyNotice.unhide()
        } else {
            emptyNotice.hide(true)
            for (preset in presets) {
                createPresetCard(preset, scrollComponent)
            }
        }
    }

    private fun createPresetCard(preset: PartyPresetData, parent: UIContainer) {
        val card = UIRoundedRectangle(4f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(4f)
            width = 100.percent()
            height = 26.pixels()
            color = UIScheme.pfCardBorder.toConstraint()
        } childOf parent

        val leftInfo = UIContainer().constrain {
            x = 6.pixels()
            y = CenterConstraint()
            width = 65.percent()
            height = ChildBasedSizeConstraint()
        } childOf card

        UIText(preset.name).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = ScaledTextConstraint(0.9f)
            height = TextAspectConstraint()
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf leftInfo

        val details = buildString {
            append(preset.island.island)
            append(" • ")
            append(preset.liquid.liquid)
            if (preset.level > 0) {
                append(" • Lvl ")
                append(preset.level)
            }
            append(" • Max ")
            append(preset.maxPlayers)
        }

        UIText(details).constrain {
            x = 0.pixels()
            y = SiblingConstraint(1f)
            width = ScaledTextConstraint(0.65f)
            height = TextAspectConstraint()
            color = UIScheme.secondaryTextColor.toConstraint()
        } childOf leftInfo

        val buttonArea = UIContainer().constrain {
            x = 4.pixels(true)
            y = CenterConstraint()
            width = 32.percent()
            height = 18.pixels()
        } childOf card

        UIButton("Load", 4f) {
            onLoadPreset?.invoke(preset)
            hideModal()
        }.constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 50.percent() - 2.pixels()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
        } childOf buttonArea

        val presetName = EmojiFeature.clearAndApplyPostStyle(preset.name, null)

        UIButton("Delete", 4f) {
            PartyFinderWindow.popup.show("Are you sure you want to delete preset \"$presetName\"?") {
                val currentEntry = getPresetsEntry()
                currentEntry.presets.remove(preset.name)
                savePresetsEntry(currentEntry)
                refreshPresetsList()
            }
        }.constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()
            width = 50.percent() - 2.pixels()
            height = 100.percent()
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfCardPresetsDeleteHoverColor.toConstraint()
            hoverTextColor = UIScheme.pfCardPresetsDeleteTextHoverColor.toConstraint()
        } childOf buttonArea
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

    override fun refreshColors() {
        this.constrain { color = backgroundColor }
        if (::modalContainer.isInitialized) modalContainer.constrain { color = primaryColor }
        if (::innerBg.isInitialized) innerBg.constrain { color = innerColor }
    }
}
