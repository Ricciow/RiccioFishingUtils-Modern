package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.GOLD
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.manager.mob.DeployableManager
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import kotlin.time.Duration

@HudElement
object FlareTimerDisplay : AbstractHudElement("flareTimerDisplay") {
    var remainingTime: Duration? = null
    var activeType: DeployableManager.FlareType = DeployableManager.FlareType.NONE

    var text : UIText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(scale)
        height = TextAspectConstraint()
    } childOf this

    override val enabled: Boolean
        get() = GeneralFishing.flareTimerDisplay && (super.enabled || remainingTime != null)

    override fun onInitialize() {
        text.setText("Flare Timer")
    }

    override fun onUpdateState() {
        text.constrain {
            width = ScaledTextConstraint(scale)
        }

        val time = remainingTime
        val finalText = if (time != null) {
            buildString {
                append("$GOLD${BOLD}Flare:")
                append(" $YELLOW${time.toReadableString()}")
                if (activeType != DeployableManager.FlareType.NONE) {
                    append(" $AQUAMARINE${activeType.bonus}")
                }
            }
        } else {
             "$GOLD${BOLD}Flare: ${YELLOW}0s"
        }

        text.setText(finalText)
    }

    fun updateTime(remaining: Duration?, type: DeployableManager.FlareType = DeployableManager.FlareType.NONE) {
        this.remainingTime = remaining
        this.activeType = type
        updateState()
    }
}