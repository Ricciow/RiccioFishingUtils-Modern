package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.gui.components.elementa.MultilineHudText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

abstract class AbstractTextHudElement(id : String) : AbstractHudElement(id) {
    override val enabled: Boolean
        get() = super.enabled

    var text : MultilineHudText = MultilineHudText(scale).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf this

    override fun onInitialize() {
        text.setText(id)
    }

    override fun onUpdateState() {
        text.updateScale(scale)
    }
}