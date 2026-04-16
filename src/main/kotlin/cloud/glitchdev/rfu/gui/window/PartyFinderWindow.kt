package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyListChangedEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerMyPartyChangedEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyCreatedEvent
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.JustifiedCramSiblingConstraint
import cloud.glitchdev.rfu.gui.components.partyfinder.UIFilterArea
import cloud.glitchdev.rfu.gui.components.partyfinder.UIPartyCard
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.model.party.Players
import cloud.glitchdev.rfu.model.party.Requisite
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.effects.ScissorEffect
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object PartyFinderWindow : BaseWindow(false) {
    val primaryColor = UIScheme.pfWindowBackground.toConstraint()

    private val headerHeight = 30.pixels
    private val filterHeight = 50.pixels
    private val spacing = 10f
    private val smallSpacing = 4f
    private var filtersOpen = false
    private var parties : List<FishingParty> = listOf(FishingParty.blankParty().apply { user = "ricciow"; title = "Title"; description = "Description"; level = 1 })
    private var partyCards : MutableList<UIPartyCard> = mutableListOf()

    val popup: UIPopup = UIPopup(5f, "", isBordered = true).childOf(window).colors {
        primaryColor = UIScheme.pfCardBorder.toConstraint()
        innerColor = UIScheme.pfCardBg.toConstraint()
        textColor = UIScheme.errorPopupColor.toConstraint()
        buttonPrimaryColor = UIScheme.pfCardBorder.toConstraint()
        buttonHoverColor = UIScheme.pfCardBorderHovered.toConstraint()
        buttonHoverTextColor = UIScheme.pfCardTitleHoverColor.toConstraint()
    }
    lateinit var filterArea : UIContainer
    lateinit var filterContainer : UIFilterArea
    lateinit var scrollArea : ScrollComponent

    init {
        create()
        onUpdate()

        registerPartyListChangedEvent { parties ->
            this.parties = parties
            println(parties)
            onUpdate()
        }

        registerPartyCreatedEvent { party ->
            //Meant to move back to the pf window after creation
            if (party.user == User.getUsername()) {
                onUpdate()
            }
        }

        registerErrorMessageEvent { message, origin ->
            if (mc.screen == this && (origin == "/app/party/join" || origin == "/app/party/report" || origin == "/app/party/delete")) {
                if (message == "Target user is not currently connected to the WebSocket.") return@registerErrorMessageEvent
                popup.setText(message)
                popup.showPopup()
            }
        }
    }

    fun create() {
        val radius = 5f

        val background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.percent
            height = 80.percent
            color = primaryColor
        } childOf window

        val useableArea = UIContainer().constrain {
            x = CenterConstraint()
            y = (radius/2).pixels
            width = 100.percent
            height = 100.percent - radius.pixels
        } childOf background

        createHeader(useableArea)
        createFilterArea(useableArea)
        createPartyArea(useableArea)

        Inspector(window) childOf window
    }

    fun createHeader(background: UIComponent) {
        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = headerHeight
        } childOf background effect ScissorEffect()

        val textScale = 1.5f

        UIText("RFU Party Finder").constrain {
            x = spacing.pixels
            y = CenterConstraint()
            width = ScaledTextConstraint(textScale)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        val rightArea = UIContainer().constrain {
            x = spacing.pixels(true)
            y = CenterConstraint()
            width = 30.percent
            height = 100.percent - 5.pixels
        } childOf header

        val createImage = UIImage.ofResource("/assets/rfu/ui/edit.png")
        UIButton.withImage(createImage, 5f) {
            //Open Party creation window (will be a new window)
        }.constrain {
            x = SiblingConstraint(5f, alignOpposite = true)
            y = CenterConstraint()
            height = 100.percent
            width = AspectConstraint(1f)
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf rightArea

        val filterImage = UIImage.ofResource("/assets/rfu/ui/filter.png")
        UIButton.withImage(filterImage, 5f) {
            filtersOpen = !filtersOpen
            onUpdate()
        }.constrain {
            x = SiblingConstraint(2f, alignOpposite = true)
            y = CenterConstraint()
            height = 100.percent
            width = AspectConstraint(1f)
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf rightArea
    }

    fun createFilterArea(background: UIComponent) {
        filterArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = 1.pixels
        } childOf background effect ScissorEffect()

        filterContainer = UIFilterArea(filterHeight) {
            onUpdate()
        }.constrain {
            x = CenterConstraint()
            y = 0.pixels
            width = 100.percent
            height = ChildBasedSizeConstraint()
        } childOf filterArea

        //Separator
        UIBlock().constrain {
            x = CenterConstraint()
            y = 0.pixels(true)
            width = 100.percent - spacing.pixels
            height = 1.pixels
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf filterArea
    }

    fun createPartyArea(background: UIComponent) {
        val partiesContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent - (2 * spacing).pixels
            height = FillConstraint()
        } childOf background

        scrollArea = ScrollComponent().constrain {
            x = CenterConstraint()
            y = smallSpacing.pixels
            width = 100.percent
            height = 100.percent - smallSpacing.pixels
        } childOf partiesContainer

        val scrollBar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 3.pixels
        } childOf partiesContainer

        scrollArea.setScrollBarComponent(scrollBar, hideWhenUseless = true, isHorizontal = false)

        updateFiltering()
    }

    fun updateFiltering() {
        if (!::scrollArea.isInitialized) return

        val currentFilteredParties = if (filtersOpen) filterContainer.applyFilter(parties) else parties
        val existingCardsMap = partyCards.associateBy { it.party.user }

        val newCards = currentFilteredParties.map { party ->
            val existingCard = existingCardsMap[party.user]
            if (existingCard?.party === party) {
                existingCard
            } else {
                UIPartyCard(party, 5f).constrain {
                    x = JustifiedCramSiblingConstraint(2f)
                    y = JustifiedCramSiblingConstraint()
                    width = 33.percent
                }
            }
        }

        if (newCards != partyCards) {
            scrollArea.clearChildren()
            newCards.forEach { it childOf scrollArea }

            partyCards.clear()
            partyCards.addAll(newCards)
        }
    }

    fun onUpdate() {
        if(filtersOpen) {
            filterArea.animate {
                setHeightAnimation(Animations.OUT_EXP, 0.5f, ChildBasedSizeConstraint())
            }
        } else {
            filterArea.animate {
                setHeightAnimation(Animations.OUT_EXP, 0.5f, 1.pixels)
            }
        }
        updateFiltering()
    }
}