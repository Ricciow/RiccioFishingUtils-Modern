package cloud.glitchdev.rfu.gui.components.partyfinder

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels

class UITitledSection(val title : String) : UIContainer() {
    init {
        create()
    }

    fun create() {
        UIText(title).constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
        } childOf this
    }

    fun addSection(component: UIComponent) {
        component.constrain {
            x = 0.pixels()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint() - 2.pixels()
        } childOf this
    }
}