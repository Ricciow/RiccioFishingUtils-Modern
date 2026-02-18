package cloud.glitchdev.rfu.gui.hud

import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

abstract class AbstractTextHudElement(id : String) : AbstractHudElement(id) {
    override val enabled: Boolean
        get() = super.enabled

    var text : UIText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(scale)
        height = TextAspectConstraint()
    } childOf this

    override fun onInitialize() {
        text.setText(id)
    }

    override fun onUpdateState() {
        text.constrain {
            width = ScaledTextConstraint(scale)
        }
    }
}