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
object XPHDisplay : AbstractTextHudElement("xphDisplay") {
    var rate = 0L
    var overallRate = 0L
    var totalXp = 0.0
    var timeElapsed = Duration.ZERO
    val isFishing: Boolean
        get() = timeElapsed != Duration.ZERO

    override val enabled: Boolean
        get() = GeneralFishing.xphDisplay && (super.enabled || !GeneralFishing.xphOnlyWhenFishing || isFishing)

    override fun onUpdateState() {
        super.onUpdateState()

        val finalText = buildString {
            append("$CYAN${BOLD}Xp/h:")                            // Xp/h label
            append(" $YELLOW${formatXp(rate)}")                    // Rate
            append(" $CYAN($YELLOW${formatXp(totalXp.toLong())}$CYAN)") // Total
            if (GeneralFishing.xphTimer) append(" $YELLOW${timeElapsed.toReadableString()}")
            if (GeneralFishing.xphOverall) {
                append("\n$CYAN${BOLD}Overall:")
                append(" $YELLOW${formatXp(overallRate)}")
            }
        }

        text.setText(finalText)
    }

    fun updateData(rate: Long, totalXp: Double, time: Duration) {
        this.rate = rate
        this.totalXp = totalXp
        this.timeElapsed = time
        val hoursElapsed = timeElapsed.inWholeMilliseconds / 3600000.0
        overallRate = if (hoursElapsed > 0) (totalXp / hoursElapsed).toLong() else 0L
        updateState()
    }

    private fun formatXp(value: Long): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
            value >= 1_000 -> "%.1fk".format(value / 1_000.0)
            else -> value.toString()
        }
    }
}
