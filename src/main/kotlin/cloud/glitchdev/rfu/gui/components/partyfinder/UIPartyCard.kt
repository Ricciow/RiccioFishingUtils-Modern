package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.CenteredPixelConstraint
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.group.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.div
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint

class UIPartyCard(
    val party: FishingParty,
    val radiusProps: Float,
    var postConfirmationText: String? = null
) : UIRoundedRectangle(radiusProps) {
    val borderWidth = UIScheme.pfCardBorderWidth
    val innerPadding = UIScheme.pfCardInnerPadding
    lateinit var titleText : UIText
    lateinit var innerContainer : UIContainer
    lateinit var levelBadge : UIPartyBadge
    lateinit var memberBadge : UIPartyBadge
    lateinit var descriptionSeparator : UIBlock
    lateinit var overlayButton : UIButton

    init {
        create()
    }

    fun create() {
        val joinErrorPopup = PartyFinderWindow.popup

        this.constrain {
            color = UIScheme.pfCardBorder.toConstraint()
            height = BoundingBoxConstraint() + borderWidth.pixels //Not 2x because bounding box accounts for padding
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
            levelBadge.animateHover()
            memberBadge.animateHover()
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparatorHover.toConstraint())
            }
            overlayButton.unhide()
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleColor.toConstraint())
            }
            levelBadge.animateNormal()
            memberBadge.animateNormal()
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparator.toConstraint())
            }
            overlayButton.hide()
        }.onMouseClick {
            if (!WebSocketClient.isConnected) {
                joinErrorPopup.show("Not connected to RFU Backend!")
                return@onMouseClick
            } else if (party.user.isUser()) {
                joinErrorPopup.show("You are already the leader of this party!")
            } else if (Party.members.contains(party.user)) {
                joinErrorPopup.show("You are already in this party!")
            } else if (party.players.current >= party.players.max) {
                joinErrorPopup.show("This party is full!")
            } else if (PartyWebSocket.myParty != null) {
                joinErrorPopup.show("You are already hosting a party! Please delete your current party listing before joining another.")
            } else {
                PartyWebSocket.joinParty(party.user)
            }
        }

        val innerBg = UIRoundedRectangle(radiusProps).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels
            width = 100.percent - (borderWidth * 2).pixels
            height = ChildBasedSizeConstraint() + (innerPadding * 2).pixels
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        innerContainer = UIContainer().constrain {
            x = innerPadding.pixels
            y = innerPadding.pixels
            width = 100.percent - (innerPadding * 2).pixels
            height = BoundingBoxConstraint()
        } childOf innerBg

        createHeader()
        createDescription()
        createTags()
        createFloating()
    }

    fun createHeader() {
        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = BoundingBoxConstraint()
        } childOf innerContainer

        val textContainer = UIContainer().constrain {
            x = 0.pixels
            y = 0.pixels
            width = BoundingBoxConstraint()
            height = BoundingBoxConstraint()
        } childOf header

        val userText = UIText(party.user) childOf textContainer
        val title = party.title.ifEmpty { party.island.island }

        titleText = UIText("§l$title").constrain {
            x = 0.pixels
            y = SiblingConstraint(UIScheme.pfCardSmallPadding)
            width = MinConstraint(ScaledTextConstraint(1f), 70.percent)
            height = TextAspectConstraint()
        } childOf textContainer

        userText.constrain {
            x = 0.pixels
            y = SiblingConstraint()
            width = TextAspectConstraint()
            height = CopyComponentSizeConstraint(titleText) * 0.75
            color = UIScheme.pfCardUserColor.toConstraint()
        }

        memberBadge = UIPartyBadge("PARTY", party.players.getString(), titleText).constrain {
            y = 0.pixels
            x = 0.pixels(true)
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(textContainer)
        } childOf header

        levelBadge = UIPartyBadge("LEVEL", "${party.level}", titleText).constrain {
            y = 0.pixels
            x = SiblingConstraint(UIScheme.pfCardSmallPadding, alignOpposite = true)
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(textContainer)
        } childOf header


        if(party.level == 0) {
            levelBadge.hide()
        }
    }

    fun createDescription() {
        val descriptionContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
            height = GroupMaxSizeConstraint("PartyCardDescription", BoundingBoxConstraint())
        } childOf innerContainer

        descriptionSeparator = UIBlock() childOf descriptionContainer

        val descriptionText = party.description.ifEmpty { "${party.island.island} fishing." }

        val description = UIWrappedText(descriptionText).constrain {
            x = SiblingConstraint(UIScheme.pfCardSmallPadding)
            y = UIScheme.pfCardSmallPadding.pixels
            width = FillConstraint()
            height = TextWrappingConstraint()
            textScale = CopyComponentSizeConstraint(titleText) * 0.75 / 9
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf descriptionContainer

        descriptionSeparator.constrain {
            x = SiblingConstraint(UIScheme.pfCardSmallPadding)
            y = 0.pixels
            width = 1.pixels
            height = CopyComponentSizeConstraint(description) + (UIScheme.pfCardSmallPadding * 2).pixels
            color = UIScheme.pfCardSeparator.toConstraint()
        }
    }

    fun createTags() {
        val tagsContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
            height = GroupMaxSizeConstraint("PartyCardTags", BoundingBoxConstraint())
        } childOf innerContainer

        UIConditionCard(DataOption("location", party.island.island)).constrain {
            x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
        } childOf tagsContainer

        if(party.liquid == LiquidTypes.WATER) {
            UIConditionCard(
                DataOption("water", "Water")
            ).constrain {
                x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
                y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            } childOf tagsContainer
        } else {
            UIConditionCard(
                DataOption("lava", "Lava")
            ).constrain {
                x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
                y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            } childOf tagsContainer
        }

        party.requisites.forEach { requisite ->
            if(requisite.value) {
                UIConditionCard(requisite.toDataOption()).constrain {
                    x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
                    y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
                } childOf tagsContainer
            }
        }
    }

    fun createFloating() {
        val isUser = party.user.isUser()
        val icon = if(isUser) "delete" else "report"
        val image = UIImage.ofResource("/assets/rfu/ui/$icon.png")
        overlayButton = UIButton.withImage(image, 5f, isBordered = true) {
            val action = if (isUser) "delete your party" else "report ${party.user}'s party"
            val pcText = postConfirmationText ?: if (isUser) null else "Party reported"
            PartyFinderWindow.popup.show("Are you sure you want to $action?", pcText) {
                if (isUser) {
                    PartyWebSocket.deleteParty(party.user)
                } else {
                    PartyWebSocket.reportParty(party.user)
                }
            }
        }.constrain {
            x = CenteredPixelConstraint(0f, true)
            y = CenteredPixelConstraint(0f)
            height = AspectConstraint()
            width = 10.percent
        } childOf this
        overlayButton.colors {
            primaryColor = UIScheme.pfCardBorder.toConstraint()
            hoverColor = UIScheme.pfCardBorderHovered.toConstraint()
            hoverTextColor = UIScheme.pfCardOverlayHoverColor.toConstraint()
        }

        overlayButton.isFloating = true
        overlayButton.hide(true)

        overlayButton.onMouseLeave {
            if(!this@UIPartyCard.isHovered()) {
                hide()
            }
        }
    }
}