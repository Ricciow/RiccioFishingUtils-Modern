package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import kotlin.time.Duration

@HudElement
object SCHDisplay : AbstractHudElement("schDisplay") {
    var rate = 0
    var total = 0
    var timeElapsed = Duration.ZERO

    var text : UIText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ScaledTextConstraint(scale)
        height = TextAspectConstraint()
    } childOf this

    override val enabled: Boolean
        get() = GeneralFishing.schDisplay

    override fun onInitialize() {
        //Required because otherwise the width is sized incorrectly
        text.setText("SCH Display")
    }

    override fun onUpdateState() {
        text.constrain {
            width = ScaledTextConstraint(scale)
        }

        val finalText = buildString {
            append("$CYAN${BOLD}Sc/h:")          //Sc/h
            append(" $YELLOW$rate")              //Number
            append(" $CYAN($YELLOW$total$CYAN)") //Total
            if(GeneralFishing.schTimer) append(" $YELLOW${timeElapsed.toReadableString()}")
        }

        text.setText(finalText)
    }

    fun updateData(rate : Int, total : Int, time : Duration) {
        this.rate = rate
        this.total = total
        this.timeElapsed = time
        updateState()
    }
}