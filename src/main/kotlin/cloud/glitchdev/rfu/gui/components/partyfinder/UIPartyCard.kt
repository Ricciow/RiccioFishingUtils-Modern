package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
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

class UIPartyCard(val party: FishingParty, val radiusProps: Float) : UIRoundedRectangle(radiusProps) {
    val borderWidth = UIScheme.pfCardBorderWidth
    val innerPadding = UIScheme.pfCardInnerPadding
    lateinit var titleText : UIText
    lateinit var innerContainer : UIContainer
    lateinit var levelBorder : UIRoundedRectangle
    lateinit var levelText : UIText
    lateinit var descriptionSeparator : UIBlock

    init {
        create()
    }

    fun create() {
        val joinErrorPopup = UIPopup(5f, "") childOf this

        this.constrain {
            color = UIScheme.pfCardBorder.toConstraint()
            height = ChildBasedSizeConstraint() + (borderWidth * 2).pixels
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
            levelText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
            levelBorder.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardLevelBorderHoveredColor.toConstraint())
            }
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparatorHover.toConstraint())
            }
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleColor.toConstraint())
            }
            levelText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleColor.toConstraint())
            }
            levelBorder.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardLevelBorderColor.toConstraint())
            }
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparator.toConstraint())
            }
        }.onMouseClick {
            if (!WebSocketClient.isConnected) {
                joinErrorPopup.setText("Not connected to RFU Backend!")
                joinErrorPopup.showPopup()
                return@onMouseClick
            } else if (party.user.isUser()) {
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

        titleText = UIText("§l${party.title}").constrain {
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

        levelBorder = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            y = 0.pixels
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(textContainer)
            color = UIScheme.pfCardLevelBorderColor.toConstraint()
        } childOf header

        val levelContainer = UIRoundedRectangle(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - 2.pixels
            height = 100.percent - 2.pixels
            color = UIScheme.pfCardLevelBgColor.toConstraint()
        } childOf levelBorder

        UIText("LVL").constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels
            width = TextAspectConstraint()
            height = CopyComponentSizeConstraint(titleText) * 0.75
            color = UIScheme.pfCardLevelLabelColor.toConstraint()
        } childOf levelContainer

        levelText = UIText("${party.level}").constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 1.pixels
            width = TextAspectConstraint()
            height = CopyComponentSizeConstraint(titleText) * 0.85
        } childOf levelContainer

        if(party.level == 0) {
            levelBorder.hide()
        }
    }

    fun createDescription() {
        val descriptionContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
            height = BoundingBoxConstraint()
        } childOf innerContainer

        descriptionSeparator = UIBlock() childOf descriptionContainer

        val description = UIWrappedText(party.description).constrain {
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

    }

    fun createFloating() {

    }
}