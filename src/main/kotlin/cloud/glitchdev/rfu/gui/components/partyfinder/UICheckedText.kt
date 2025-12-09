package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.party.Requisite
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class UICheckedText(val requisite: Requisite) : UIContainer() {
    init {
        create()
    }

    private fun getStatusSymbol() : String {
        return (if (requisite.value) "✔" else "❌")
    }

    private fun getStatusColor() : ColorConstraint {
        return (if (requisite.value) UIScheme.allowColor else UIScheme.denyColor).toConstraint()
    }

    fun create() {
        UIText(requisite.name).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            height = TextAspectConstraint()
            width = ScaledTextConstraint(1f)
        } childOf this

        UIText(getStatusSymbol()).constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            height = TextAspectConstraint()
            width = ScaledTextConstraint(1f)
            color = getStatusColor()
        } childOf this
    }
}