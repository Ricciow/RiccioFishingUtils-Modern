package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.gui.components.Button
import gg.essential.elementa.components.UIRoundedRectangle
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
            color = ColorScheme.primaryColorOpaque.toConstraint()
        } childOf window

        Button("Testando Botao longo para caralho", 5f) {

        }.constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 20.percent()
            height = 20.pixels()
        } childOf background
    }
}