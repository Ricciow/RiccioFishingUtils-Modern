package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.fishing.FishTrackingType
import cloud.glitchdev.rfu.feature.fishing.FishingXpTracker
import cloud.glitchdev.rfu.feature.mob.SeaCreatureHour
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@HudElement
object FishTrackingDisplay : AbstractTextHudElement("fishTrackingDisplay") {

    private val isFishing: Boolean
        get() = SeaCreatureHour.startFishing != Instant.DISTANT_PAST || FishingXpTracker.startFishing != Instant.DISTANT_PAST

    override val enabled: Boolean
        get() = GeneralFishing.fishTrackingDisplay && (super.enabled || !GeneralFishing.fishTrackingOnlyWhenFishing || isFishing)

    override fun onUpdateState() {
        super.onUpdateState()

        val lines = mutableListOf<String>()
        val items = GeneralFishing.fishTrackingItems

        val time = getTimeElapsed()

        if (items.contains(FishTrackingType.SC_H)) {
            val rate = SeaCreatureHour.currentScPerHour.toInt()
            val total = SeaCreatureHour.total
            val line = buildString {
                append("$CYAN${BOLD}SC/h:")
                append(" $YELLOW$rate")
                append(" $CYAN($YELLOW$total$CYAN)")
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = getOverallScRate(time)
                    append(" $CYAN[$YELLOW${overall}$CYAN]")
                }
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.XP_H)) {
            val rate = FishingXpTracker.currentXpPerHour.toLong()
            val total = FishingXpTracker.totalXp.toLong()
            val line = buildString {
                append("$CYAN${BOLD}XP/h:")
                append(" $YELLOW${formatXp(rate)}")
                append(" $CYAN($YELLOW${formatXp(total)}$CYAN)")
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = getOverallXpRate(time)
                    append(" $CYAN[$YELLOW${formatXp(overall)}$CYAN]")
                }
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.TIMER) && (time != Duration.ZERO || isEditing)) {
            lines.add("$CYAN${BOLD}Timer: $YELLOW${time.toReadableString()}")
        }

        text.setText(if (lines.isEmpty()) "fishTrackingDisplay" else lines.joinToString("\n"))
    }

    private fun getTimeElapsed(): Duration {
        val now = Clock.System.now()
        val scTime = if (SeaCreatureHour.startFishing != Instant.DISTANT_PAST)
            now - SeaCreatureHour.startFishing
        else Duration.ZERO
        val xpTime = if (FishingXpTracker.startFishing != Instant.DISTANT_PAST)
            now - FishingXpTracker.startFishing
        else Duration.ZERO

        return if (scTime > xpTime) scTime else xpTime
    }

    private fun getOverallScRate(time: Duration): Int {
        val hoursElapsed = time.inWholeMilliseconds / 3600000.0
        return if (hoursElapsed > 0) (SeaCreatureHour.total / hoursElapsed).toInt() else 0
    }

    private fun getOverallXpRate(time: Duration): Long {
        val hoursElapsed = time.inWholeMilliseconds / 3600000.0
        return if (hoursElapsed > 0) (FishingXpTracker.totalXp / hoursElapsed).toLong() else 0L
    }

    private fun formatXp(value: Long): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
            value >= 1_000 -> "%.1fk".format(value / 1_000.0)
            else -> value.toString()
        }
    }
}
