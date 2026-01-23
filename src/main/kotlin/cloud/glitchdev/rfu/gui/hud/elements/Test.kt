package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

@HudElement
object Test : AbstractHudElement("Testing") {
    var text : UIText = UIText("Ola").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(scale)
        height = TextAspectConstraint()
    } childOf this

    override val enabled: Boolean
        get() = GeneralFishing.lootshareRange

    override fun onUpdateState() {
        text.constrain {
            width = ScaledTextConstraint(scale)
        }
    }
}