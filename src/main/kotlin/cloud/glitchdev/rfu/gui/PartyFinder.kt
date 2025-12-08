package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.UICheckbox
import cloud.glitchdev.rfu.gui.components.dropdown.DropdownOption
import cloud.glitchdev.rfu.gui.components.dropdown.UIDropdown
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class PartyFinder : BaseWindow() {
    init {
        create()
    }

    fun create() {
        val background = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(0.8f)
            height = RelativeWindowConstraint(0.8f)
            color = UIScheme.primaryColorOpaque.toConstraint()
        } childOf window

        UIButton("Testando Botao longo", 5f) {

        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 20.percent()
            height = 20.pixels()
        } childOf background

        UICheckbox("Checkbox") {

        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 20.percent()
            height = 20.pixels()
        } childOf background

        val drop = UIDropdown(arrayListOf(DropdownOption("Teste", "Teste"), DropdownOption("Teste2", "Teste2")), 0, 2f, window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 20.percent()
            height = 20.pixels()
        } childOf background

        drop.updateDropdown()

        Inspector(window) childOf window
    }
}