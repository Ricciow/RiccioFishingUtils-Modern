package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.gui.components.UICheckbox
import cloud.glitchdev.rfu.gui.components.UIDecoratedTextInput
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels

class UIFilterArea(radius: Float) : UIRoundedRectangle(radius) {
    init {
        create()
    }

    fun create() {
        val topArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = 50.percent()
        } childOf this

        UIDropdown(FishingIslands.toDropdownOptions(), 0, 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 25.percent()
            height = 50.percent()
        } childOf topArea

        UIDropdown(PartyTypes.toDropdownOptions(), 0, 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 25.percent()
            height = 50.percent()
        } childOf topArea

        UIDecoratedTextInput("Sea Creature", 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = FillConstraint() - 6.pixels()
            height = 50.percent()
        } childOf topArea

        UIDecoratedTextInput("LVL", 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = 10.percent()
            height = 50.percent()
        } childOf topArea

        val bottomArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 96.percent()
            height = 50.percent()
        } childOf this

        UICheckbox("Can Join").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Lava").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Water").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Has Killer").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Enderman 9").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Looting 5").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea

        UICheckbox("Brain Food").constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint()
            width = ChildBasedSizeConstraint()
            height = 50.percent()
        } childOf bottomArea
    }
}