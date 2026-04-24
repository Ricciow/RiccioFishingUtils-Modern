package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.FishTrackingType
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

import cloud.glitchdev.rfu.feature.fishing.FishingSession
import kotlin.time.Duration.Companion.minutes

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
        val time = FishingSession.duration

        if (items.contains(FishTrackingType.SC_H)) {
            val rate = FishingSession.scTracker.currentRatePerHour.toInt()
            val total = FishingSession.scTracker.total.toInt()
            val line = buildString {
                append("$CYAN${BOLD}SC/h:")
                append(" $YELLOW$rate")
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = FishingSession.scTracker.overallRatePerHour.toInt()
                    append(" $CYAN[$YELLOW${overall}$CYAN]")
                }
                append(" $CYAN($YELLOW$total$CYAN)")
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.XP_H)) {
            val rate = FishingSession.xpTracker.currentRatePerHour.toLong()
            val total = FishingSession.xpTracker.total.toLong()
            val line = buildString {
                append("$CYAN${BOLD}XP/h:")
                append(" $YELLOW${formatXp(rate)}")
                if (items.contains(FishTrackingType.OVERALL)) {
                    val overall = FishingSession.xpTracker.overallRatePerHour.toLong()
                    append(" $CYAN[$YELLOW${formatXp(overall)}$CYAN]")
                }
                append(" $CYAN($YELLOW${formatXp(total)}$CYAN)")
            }
            lines.add(line)
        }

        if (items.contains(FishTrackingType.TIMER) && (time != Duration.ZERO || isEditing)) {
            var line = "$CYAN${BOLD}Timer: $YELLOW${time.toReadableString()}"
            if (FishingSession.isPaused) {
                line += " $CYAN(${LIGHT_RED}Paused$CYAN)"
            }
            lines.add(line)
        } else if(FishingSession.isPaused) {
            lines.add("$CYAN(${LIGHT_RED}Paused$CYAN)")
        }

        text.setText(if (lines.isEmpty()) {
            if (isEditing) "fishTrackingDisplay" else ""
        } else lines.joinToString("\n"))
    }

    private fun formatXp(value: Long): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
            value >= 1_000 -> "%.1fk".format(value / 1_000.0)
            else -> value.toString()
        }
    }
}
