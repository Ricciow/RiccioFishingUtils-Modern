package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.MultilineHudText
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

abstract class AbstractTextHudElement(id : String) : AbstractHudElement(id) {
    override val enabled: Boolean
        get() = super.enabled

    var container = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = BoundingBoxConstraint()
        height = BoundingBoxConstraint()
    } childOf this

    var text : MultilineHudText = MultilineHudText(scale).constrain {
        x = SiblingConstraint()
        y = SiblingConstraint()
    } childOf container

    override fun onInitialize() {
        text.setText(id)
    }

    override fun onUpdateState() {
        text.updateScale(scale)
    }
}