package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.Dyes as ConstDyes
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.text.TextEffects.RESET
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.model.dye.Dyes
import cloud.glitchdev.rfu.utils.network.DyeHttp
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

@HudElement
object DyeDisplay : AbstractHudElement("dyeDisplay") {
    override val enabled: Boolean
        get() = OtherSettings.dyeDisplay && !currentDyes.isOutdated()

    private val currentDyes : Dyes
        get() = DyeHttp.currentDyes ?: Dyes()

    private val container = UIContainer().constrain {
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    private var dyeLines: List<UIText> = emptyList()

    override fun onUpdateState() {
        dyeLines.forEach { container.removeChild(it) }

        val entries: List<Pair<String, String>> = if (currentDyes.isOutdated()) {
            listOf("" to "Unknown Dyes")
        } else {
            buildList {
                add("${TextColor.YELLOW}${BOLD}3x$RESET " to currentDyes.get3xDye())
                currentDyes.get2xDyes().forEach { add("${TextColor.YELLOW}${BOLD}2x$RESET " to it) }
            }
        }

        dyeLines = entries.mapIndexed { _, (label, dyeName) ->
            val dye = ConstDyes.getRelatedDye(dyeName)
            val color = if (dye != null) {
                Color(dye.hex.toInt(16))
            } else {
                Color(255, 85, 85)
            }
            val displayName = dyeName.removeSuffix(" Dye")
            UIText("$label$displayName").constrain {
                y = SiblingConstraint()
                width = ScaledTextConstraint(scale)
                height = TextAspectConstraint()
                this.color = color.toConstraint()
            } childOf container
        }
    }
}