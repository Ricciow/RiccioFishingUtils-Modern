package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyListChangedEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerMyPartyChangedEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyCreatedEvent
import cloud.glitchdev.rfu.events.managers.ErrorEvents.registerErrorMessageEvent
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.gui.components.partyfinder.UIFilterArea
import cloud.glitchdev.rfu.gui.components.partyfinder.UIPartyCard
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.User
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
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

object PartyFinderWindow : BaseWindow(false) {
    val primaryColor = UIScheme.pfWindowBackground.toConstraint()

    private val headerHeight = 30.pixels
    private val filterHeight = 50.pixels
    private val spacing = 10f
    private var filtersOpen = false

    lateinit var popup: UIPopup
    lateinit var createButton: UIButton
    lateinit var filterArea : UIContainer
    lateinit var filterContainer : UIFilterArea

    init {
        create()
        onUpdate()

        registerPartyListChangedEvent { parties ->
            onUpdate()
        }

        registerPartyCreatedEvent { party ->
            if (party.user == User.getUsername()) {
                onUpdate()
            }
        }

        registerMyPartyChangedEvent {
            onUpdate()
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
        createButton = UIButton.withImage(createImage, 5f) {
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
        createButton = UIButton.withImage(filterImage, 5f) {
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
            println("Filter Changed")
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

        val party = FishingParty.blankParty()

        party.user = "ricciow"
        party.title = "Super Cool Title"
        party.description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean congue auctor semper. Proin egestas tincidunt finibus. Donec venenatis neque non orci vulputate, ut malesuada justo lobortis massa nunc."
        party.level = 1

        UIPartyCard(party, 5f).constrain {
            width = 33.percent
        } childOf partiesContainer
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
    }
}