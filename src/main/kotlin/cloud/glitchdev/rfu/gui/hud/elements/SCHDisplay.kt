package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Duration

@HudElement
object SCHDisplay : AbstractTextHudElement("schDisplay") {
    var rate = 0
    var overallRate = 0
    var total = 0
    var timeElapsed = Duration.ZERO
    val isFishing : Boolean
        get() = timeElapsed != Duration.ZERO

    override val enabled: Boolean
        get() = GeneralFishing.schDisplay && (super.enabled || !GeneralFishing.schOnlyWhenFishing || isFishing)

    override fun onUpdateState() {
        super.onUpdateState()

        val finalText = buildString {
            append("$CYAN${BOLD}Sc/h:")          //Sc/h
            append(" $YELLOW$rate")              //Number
            append(" $CYAN($YELLOW$total$CYAN)") //Total
            if(GeneralFishing.schTimer) append(" $YELLOW${timeElapsed.toReadableString()}")
            if(GeneralFishing.schOverall) {
                append("\n$CYAN${BOLD}Overall:")
                append(" $YELLOW$overallRate")
            }
        }

        text.setText(finalText)
    }

    fun updateData(rate : Int, total : Int, time : Duration) {
        this.rate = rate
        this.total = total
        this.timeElapsed = time
        val hoursElapsed = timeElapsed.inWholeMilliseconds / 3600000.0
        overallRate = if (hoursElapsed > 0) (total / hoursElapsed).toInt() else 0
        updateState()
    }
}