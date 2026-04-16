package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.checkbox.UIRadio
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import cloud.glitchdev.rfu.gui.components.dropdown.UISelectionDropdown
import cloud.glitchdev.rfu.gui.components.textinput.UIWrappedDecoratedTextInput
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerMyPartyChangedEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.utils.gui.isHidden
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UMatrixStack

class UICreateParty : UIContainer() {
    val popup: UIPopup = PartyFinderWindow.popup

    init {
        registerErrorMessageEvent { message, origin ->
            if (!this.isHidden() && (origin == "/app/party/publish" || origin == "/app/party/edit")) {
                popup.show(message)
            }
        }

        registerMyPartyChangedEvent { party ->

        }

        create()
    }

    fun create() {
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
        } childOf border
    }
}
