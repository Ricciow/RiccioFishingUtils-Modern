package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.FishTrackingType
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingXpTracker
import cloud.glitchdev.rfu.feature.mob.SeaCreatureHour
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

import cloud.glitchdev.rfu.feature.fishing.FishingSession

@HudElement
object FishTrackingDisplay : AbstractTextHudElement("fishTrackingDisplay") {

    private val isFishing: Boolean
        get() = FishingSession.isFishing

    override val enabled: Boolean
        get() = GeneralFishing.fishTrackingDisplay && (super.enabled || !GeneralFishing.fishTrackingOnlyWhenFishing || isFishing)

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

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
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = getOverallScRate(time)
                    append(" $CYAN[$YELLOW${overall}$CYAN]")
                }
                append(" $CYAN($YELLOW$total$CYAN)")
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.XP_H)) {
            val rate = FishingXpTracker.currentXpPerHour.toLong()
            val total = FishingXpTracker.totalXp.toLong()
            val line = buildString {
                append("$CYAN${BOLD}XP/h:")
                append(" $YELLOW${formatXp(rate)}")
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = getOverallXpRate(time)
                    append(" $CYAN[$YELLOW${formatXp(overall)}$CYAN]")
                }
                append(" $CYAN($YELLOW${formatXp(total)}$CYAN)")
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.TIMER) && (time != Duration.ZERO || isEditing)) {
            lines.add("$CYAN${BOLD}Timer: $YELLOW${time.toReadableString()}")
        }

        text.setText(if (lines.isEmpty()) {
            if (isEditing) "fishTrackingDisplay" else ""
        } else lines.joinToString("\n"))
    }

    private fun getTimeElapsed(): Duration {
        val now = Clock.System.now()
        val start = FishingSession.startFishing
        return if (start != Instant.DISTANT_PAST) now - start else Duration.ZERO
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
