package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.partyfinder.UICreateParty
import cloud.glitchdev.rfu.gui.components.partyfinder.UIFilterArea
import cloud.glitchdev.rfu.gui.components.partyfinder.UIPartyCard
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.setHidden
import cloud.glitchdev.rfu.utils.network.PartyHttp
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class PartyFinderWindow : BaseWindow() {
    val primaryColor = UIScheme.primaryColorOpaque.toConstraint()
    val secondaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val radius = 5f
    val windowSize = 0.8f
    var parties : MutableList<FishingParty> = mutableListOf()
    var displayParties : MutableList<FishingParty> = mutableListOf()
    val partyCards : MutableList<UIPartyCard> = mutableListOf()

    lateinit var background : UIRoundedRectangle
    lateinit var filterArea : UIFilterArea
    lateinit var partyCreationArea : UICreateParty
    lateinit var partyArea : UIContainer
    lateinit var scrollArea : ScrollComponent
    lateinit var filterButton : UIButton
    lateinit var reloadButton : UIButton
    lateinit var partyCreationButton : UIButton

    init {
        create()
        updatePartyCreation()
        getParties()
    }

    fun getParties() {
        reloadButton.disabled = true
        parties.clear()
        updateFiltering()
        PartyHttp.getExistingParties { newParties ->
            minecraft.execute {
                parties.addAll(newParties)
                updateFiltering()
                if(!partyCreationOpen) reloadButton.disabled = false
            }
        }
    }

    fun updateFiltering() {
        if(::scrollArea.isInitialized) {
            displayParties = if (filterOpen) filterArea.applyFilter(parties) else parties
            for (partyCard in partyCards) {
                scrollArea.removeChild(partyCard)
            }

            partyCards.clear()

            for(party in displayParties) {
                val partyCard = UIPartyCard(party, 5f) {
                    getParties()
                }.constrain {
                    x = 0.pixels()
                    y = SiblingConstraint(2f)
                    width = 100.percent()
                    height = 80.pixels()
                } childOf scrollArea

                partyCards.add(partyCard)
            }
        }
    }

    fun create() {
        background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(windowSize)
            height = max(RelativeWindowConstraint(windowSize), 220.pixels())
            color = primaryColor
        } childOf window

        createHeader()

        partyCreationArea = UICreateParty(5f) { success ->
            if(success) {
                partyCreationOpen = false
                updatePartyCreation()
                getParties()
            }
        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint() - 2f.pixels()
            color = primaryColor
        } childOf background

        filterArea = UIFilterArea(radius).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = max(20.percent(), 40.pixels())
            color = primaryColor
        } childOf background

        filterArea.onFilterChange = {
            updateFiltering()
        }

        createPartyArea()
    }

    fun createPartyArea() {
        partyArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 96.percent()
            height = FillConstraint() - 6.pixels()
        } childOf background

        val scrollbar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 5.pixels()
            color = secondaryColor
        } childOf partyArea

        scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 100.percent() - 7.pixels()
            height = 100.percent()
        } childOf partyArea

        scrollArea.setScrollBarComponent(scrollbar, false, false)
    }

    fun createHeader() {
        val header = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = max(10.percent(), 20.pixels())
            color = primaryColor
        } childOf background

        UIText("RFU Party Finder").constrain {
            x = 2.percent()
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = 50.percent()
            color = textColor
        } childOf header

        val rightContainer = UIContainer().constrain {
            x = 98.percent()
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 80.percent()
        } childOf header

        partyCreationButton = UIButton("New Party", 3f) {
            partyCreationOpen = !partyCreationOpen
            updatePartyCreation()
        }.constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = 70.pixels()
            height = 100.percent()
        } childOf rightContainer

        filterButton = UIButton("Filters", 3f) {
            filterOpen = !filterOpen
            updatePartyCreation()
            updateFiltering()
        }.constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = 50.pixels()
            height = 100.percent()
        } childOf rightContainer

        reloadButton = UIButton("\uD83D\uDDD8", 3f) {
            getParties()
        }.constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = AspectConstraint(1f)
            height = 100.percent()
        } childOf rightContainer

        reloadButton.textArea.constrain {
            width = 60.percent()
            height = TextAspectConstraint()
        }

        rightContainer.constrain {
            x = 98.percent() - rightContainer.getWidth().pixels()
        }
    }

    fun updatePartyCreation() {
        Window.enqueueRenderOperation {
            partyCreationArea.setHidden(!partyCreationOpen)
            partyArea.setHidden(partyCreationOpen)
            filterArea.setHidden(partyCreationOpen || !filterOpen)
            partyCreationButton.setText(if (partyCreationOpen) "Close" else "New Party")
            filterButton.disabled = partyCreationOpen
            reloadButton.disabled = partyCreationOpen
        }
    }

    companion object {
        var filterOpen = false
        var partyCreationOpen = false
    }
}