package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.ink.InkSessionTracker
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.collections.CollectionItem
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.constants.InkTrackingType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.dsl.compact
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@HudElement
object InkTrackingDisplay : AbstractTextHudElement("inktrackingdisplay") {

    private val isFishing: Boolean
        get() = FishingSession.isFishing

    override val enabled: Boolean
        get() = InkFishing.inkTrackingDisplay && (World.island == FishingIslands.PARK) && (super.enabled || !InkFishing.fishTrackingOnlyWhenFishing || (isFishing && FishingSession.pausedDuration < 1.minutes))


    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val lines = mutableListOf<String>()
        val items = InkFishing.inkTrackingItems

        val time = FishingSession.duration
        val totalInk = CollectionsHandler.get(CollectionItem.INK_SAC)
        val inkRate = InkSessionTracker.currentInkPerHour.toInt()

        if(items.contains(InkTrackingType.INK_H)) {

            val inkSession = InkSessionTracker.totalInk.toInt()

            if(inkRate > 0) {
                val line = buildString {
                    append("${CYAN}${BOLD}Ink/hr:")
                    append(" $YELLOW${formatInk(inkRate.toLong())}")
                    if (items.contains(InkTrackingType.OVERALL)) {
                        val overall = FishingSession.inkTracker.overallRatePerHour.toLong()
                        append(" $CYAN[$YELLOW${formatInk(overall)}$CYAN]")
                    }
                    append(" $CYAN($YELLOW${inkSession.toLong().compact()}$CYAN)")
                }
                lines.add(line)
            }
        }

        if(items.contains(InkTrackingType.UPTIME) && (time != Duration.ZERO || isEditing)) {

            val line = buildString {
                append("$CYAN${BOLD}Uptime: $YELLOW${time.toReadableString()}")
                if(FishingSession.isPaused) append(" $CYAN(${TextColor.LIGHT_RED}Paused$CYAN)")
            }
            lines.add(line)
        }


        if(items.contains(InkTrackingType.INK_TOT)) {

            if(totalInk > 0) {
                val line = buildString {
                    append("$CYAN${BOLD}Ink Collection: $YELLOW${formatInk(totalInk)}")
                }
                lines.add(line)
            }
        }

        if(items.contains(InkTrackingType.SQUIDS)) {
            val squid = SeaCreatures.get("Squid")
            val squidNow = squid?.let { catchHistory.getOrAdd(it).total } ?: 0
            val squidGain = InkSessionTracker.squidGain

            val line = buildString {
                append("$CYAN${BOLD}Squids: $YELLOW${squidGain} $CYAN($LIGHT_GREEN${squidNow}$CYAN)")
            }
            lines.add(line)
        }

        if(items.contains(InkTrackingType.N_SQUID)) {
            val nightSquid = SeaCreatures.get("Night Squid")
            val nightSquidNow = nightSquid?.let { catchHistory.getOrAdd(it).total } ?: 0
            val nSquidGain = InkSessionTracker.nightSquidGain

            val line = buildString {
                append("$CYAN${BOLD}Night Squids: $YELLOW${nSquidGain} $CYAN($LIGHT_GREEN${nightSquidNow}$CYAN)")
            }
            lines.add(line)
        }

        val inkGoal = InkFishing.goalInk

        if(items.contains(InkTrackingType.INK_GOAL)) {

            val percentage = InkSessionTracker.percentageToGoal

            val line = buildString {
                append("$CYAN${BOLD}Ink Goal: $YELLOW${truncInk(inkGoal.toLong())}")
                if(inkRate > 0 && inkGoal > totalInk) append(" $CYAN($YELLOW${"%.1f".format(percentage)}%$CYAN)")
                else if(inkRate > 0) append(" $CYAN(${YELLOW}100%$CYAN)")
            }
            lines.add(line)

        }

        if(items.contains(InkTrackingType.ETA)) {
            val eta = InkSessionTracker.etaToGoal

            if(inkRate > 0 && inkGoal > totalInk) {
                lines.add("${CYAN}${BOLD}ETA: $YELLOW${eta.toReadableString()}")
            } else if (inkRate > 0) {
                val line = buildString {
                    append("${CYAN}${BOLD}ETA:")
                    append(" ${YELLOW}0 seconds")
                }

                lines.add(line)
            }
        }

        text.setText(if (lines.isEmpty()) {
            if (isEditing) "inktrackingdisplay" else ""
        } else lines.joinToString("\n"))
    }

    private fun formatInk(value: Long): String {
        return "%,d".format(value)
    }

    private fun truncInk(value: Long): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
            value >= 1_000 -> "%.1fk".format(value / 1_000.0)
            else -> value.toString()
        }
    }
}
