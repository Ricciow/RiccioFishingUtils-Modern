package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.gui.addHoverColoring
import cloud.glitchdev.rfu.utils.gui.setHidden
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class UIPartyCard(val party: FishingParty, radius : Float) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.decreaseOpacity(UIScheme.primaryColorOpaque, 80).toConstraint()
    val hoverColor = UIScheme.secondaryColorOpaque.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val hoverText = UIScheme.denyColor.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val fontSize = 1f

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        val joinErrorPopup = UIPopup(5f, "") childOf this

        this.onMouseClick {
            if (!cloud.glitchdev.rfu.utils.network.WebSocketClient.isConnected) {
                joinErrorPopup.setText("Not connected to RFU Backend!")
                joinErrorPopup.showPopup()
                return@onMouseClick
            }
            if (party.user.isUser()) {
                joinErrorPopup.setText("You are already the leader of this party!")
                joinErrorPopup.showPopup()
            } else if (Party.members.contains(party.user)) {
                joinErrorPopup.setText("You are already in this party!")
                joinErrorPopup.showPopup()
            } else if (party.players.current >= party.players.max) {
                joinErrorPopup.setText("This party is full!")
                joinErrorPopup.showPopup()
            } else if (PartyWebSocket.myParty != null) {
                joinErrorPopup.setText("You are already hosting a party! Please delete your current party listing before joining another.")
                joinErrorPopup.showPopup()
            } else {
                PartyWebSocket.joinParty(party.user)
            }
        }

        this.addHoverColoring(Animations.IN_EXP, hoverDuration, primaryColor, hoverColor)

        val mainContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 5.pixels()
            height = 100.percent() - 5.pixels()
        } childOf this

        val leftArea = UIContainer().constrain {
            x = SiblingConstraint()
            y = CenterConstraint()
            width = FillConstraint()
            height = 100.percent()
        } childOf mainContainer

        UIWrappedText(party.getTitleString()).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = TextWrappingConstraint()
            color = textColor
        } childOf leftArea

        UIWrappedText(party.description, trimText = true).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint() - 4.pixels()
            color = textColor
        } childOf leftArea

        UIWrappedText(party.getCountString()).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = TextWrappingConstraint()
            color = textColor
        } childOf leftArea

        val rightArea = UIContainer().constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = max(33.percent(), 180.pixels())- 2.pixels()
            height = 100.percent()
        } childOf mainContainer

        UIText("Type: ${party.fishingType.type}").constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = ScaledTextConstraint(fontSize)
            height = TextAspectConstraint()
            color = textColor
        } childOf rightArea

        val restrictions = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf rightArea

        for(requisites in party.requisites) {
            UICheckedText(requisites).constrain {
                x = CramSiblingConstraint(8f)
                y = CramSiblingConstraint(2f)
                width = 40.percent()
                height = 9.pixels()
            } childOf restrictions
        }

        UIWrappedText("SCs: ${party.getSeaCreatureString()}", trimText = true).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint()
        } childOf rightArea

        val reportPopup = UIPopup(5f, "Are you sure you want to report ${party.user}'s party?", onConfirm = {
            PartyWebSocket.reportParty(party.user)
            Chat.sendMessage(TextUtils.rfupfLiteral("Party reported", TextColor.YELLOW))
        }) childOf this

        val buttonsContainer = UIContainer().constrain {
            x = 2.pixels(true)
            y = 2.pixels()
            width = 10.pixels()
            height = ChildBasedSizeConstraint()
        } childOf mainContainer

        val reportButton = if (!party.user.isUser()) {
            UIText("⚠").constrain {
                x = SiblingConstraint(5f)
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf buttonsContainer
        } else null

        reportButton?.addHoverColoring(Animations.IN_EXP, hoverDuration, textColor, hoverText)
        reportButton?.setHidden(true)
        reportButton?.onMouseClick { event ->
            event.stopPropagation()
            reportPopup.showPopup()
        }

        val deleteButton = if (party.user.isUser()) {
            UIText("❌").constrain {
                x = SiblingConstraint(5f)
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
                isFloating = true
            } childOf buttonsContainer
        } else null

        deleteButton?.addHoverColoring(Animations.IN_EXP, hoverDuration, textColor, hoverText)
        deleteButton?.setHidden(true)
        deleteButton?.onMouseClick { event ->
            event.stopPropagation()
            PartyWebSocket.deleteParty(User.getUsername())
        }

        this.onMouseEnter {
            deleteButton?.setHidden(false)
            reportButton?.setHidden(false)
        }
        this.onMouseLeave {
            deleteButton?.setHidden(true)
            reportButton?.setHidden(true)
        }
    }
}
