package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.partyfinder.UICreateParty
import cloud.glitchdev.rfu.gui.components.partyfinder.UIFilterArea
import cloud.glitchdev.rfu.gui.components.partyfinder.UIPartyCard
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.setHidden
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
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

class PartyFinder : BaseWindow() {
    val primaryColor = UIScheme.primaryColorOpaque.toConstraint()
    val secondaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val radius = 5f
    val windowSize = 0.8f
    val parties : MutableList<FishingParty> = mutableListOf()

    lateinit var background : UIRoundedRectangle
    lateinit var filterArea : UIFilterArea
    lateinit var partyCreationArea : UICreateParty
    lateinit var partyArea : UIContainer
    lateinit var filterButton : UIButton
    lateinit var partyCreationButton : UIButton

    init {
        getParties()
        create()
        updatePartyCreation()
    }

    fun getParties() {
        parties.add(FishingParty.fromJson("{\"user\":\"Usuariotop\",\"level\":200,\"title\":\"Titulotop\",\"description\":\"Decricaotop\",\"liquid\":\"Water\",\"fishing_type\":\"Treasure\",\"island\":\"Crimson Isle\",\"requisites\":[{\"id\":\"enderman_9\",\"name\":\"Eman9\",\"value\":true},{\"id\":\"brain_food\",\"name\":\"BrainFood\",\"value\":true},{\"id\":\"looting_5\",\"name\":\"Looting5\",\"value\":true},{\"id\":\"has_killer\",\"name\":\"Haskiller\",\"value\":true}],\"sea_creatures\":[\"Jawbus\",\"Thunder\"],\"players\":{\"current\":2,\"max\":10}}"))
    }

    fun create() {
        background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(windowSize)
            height = RelativeWindowConstraint(windowSize)
            color = primaryColor
        } childOf window

        createHeader()

        partyCreationArea = UICreateParty(5f).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = 100.percent() - max(10.percent(), 20.pixels())
            color = primaryColor
        } childOf background

        partyCreationArea.setHidden(!partyCreationOpen)

        filterArea = UIFilterArea(radius).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = max(20.percent(), 40.pixels())
            color = primaryColor
        } childOf background

        filterArea.setHidden(!filterOpen)

        createPartyArea()

        Inspector(window) childOf window
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

        val scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 100.percent() - 7.pixels()
            height = 100.percent()
        } childOf partyArea

        scrollArea.setScrollBarComponent(scrollbar, false, false)

        for(party in parties) {
            UIPartyCard(party, 5f).constrain {
                x = 0.pixels()
                y = SiblingConstraint(2f)
                width = 100.percent()
                height = 80.pixels()
            } childOf scrollArea
        }
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
            filterArea.setHidden(!filterOpen)
        }.constrain {
            x = SiblingConstraint(2f, true)
            y = CenterConstraint()
            width = 50.pixels()
            height = 100.percent()
        } childOf rightContainer

            rightContainer.constrain {
            x = 98.percent() - rightContainer.getWidth().pixels()
        }


    }

    fun updatePartyCreation() {
        partyCreationArea.setHidden(!partyCreationOpen)
        partyArea.setHidden(partyCreationOpen)
        filterArea.setHidden(if (partyCreationOpen) true else !filterOpen)
        partyCreationButton.setText(if (partyCreationOpen) "Close" else "New Party")
        filterButton.disabled = partyCreationOpen
    }

    companion object {
        var filterOpen = false
        var partyCreationOpen = false
    }
}