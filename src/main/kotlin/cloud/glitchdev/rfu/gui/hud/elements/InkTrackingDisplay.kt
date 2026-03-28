package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN

import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.text.TextColor.RED
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingXpTracker
import cloud.glitchdev.rfu.feature.mob.SeaCreatureHour
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.ink.CollectionHour
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.constants.SeaCreatures.NIGHT_SQUID
import cloud.glitchdev.rfu.constants.SeaCreatures.SQUID
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.data.fishing.InkTrackingType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HudElement
object InkTrackingDisplay : AbstractTextHudElement("inktrackingdisplay") {

    private val isFishing: Boolean
        get() = FishingSession.isFishing

    override val enabled: Boolean
        get() = InkFishing.inkTrackingDisplay && (World.map == "The Park") && (super.enabled || !InkFishing.fishTrackingOnlyWhenFishing|| isFishing)

    val nightSquidStart = catchHistory.getOrAdd(NIGHT_SQUID).total
    val squidStart = catchHistory.getOrAdd(sc=SQUID).total


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

        val time = CollectionHour.effectiveElapsed

        val totalInk = CollectionsHandler.totalInkSac
        val inkRate = CollectionHour.currentInkPerHour.toInt()


        if(items.contains(InkTrackingType.INK_H)) {

            val inkSession = CollectionHour.totalInk.toInt()

            if(inkRate > 0) {
                val line = buildString {
                    append("${CYAN}${BOLD}Ink/hr:")
                    append(" $YELLOW${formatInk(inkRate.toLong())}")
                    append(" $CYAN($YELLOW$inkSession$CYAN)")
                }
                lines.add(line)
            }
        }

        if(items.contains(InkTrackingType.UPTIME) && (time != Duration.ZERO || isEditing)) {

            val line = buildString {
                append("$CYAN${BOLD}Uptime: $YELLOW${time.toReadableString()}")
                if(CollectionHour.pausedAt != null) append(" $CYAN(${RED}Paused$CYAN)")
            }
            lines.add(line)
        }


        if(items.contains(InkTrackingType.INK_TOT)) {

            if(totalInk > 0) {
                val line = buildString {
                    append("$CYAN${BOLD}Ink Collection: $YELLOW${formatInk(totalInk.toLong())}")
                }
                lines.add(line)
            }
        }

        if(items.contains(InkTrackingType.SQUIDS)) {

            val squidNow = catchHistory.getOrAdd(sc=SQUID).total
            val squidGain = squidNow - squidStart

            val line = buildString {
                append("$CYAN${BOLD}Squids: $YELLOW${squidGain} $CYAN($LIGHT_GREEN${squidNow}$CYAN)")
            }
            lines.add(line)

        }

        if(items.contains(InkTrackingType.N_SQUID)) {

            val nightSquidNow = catchHistory.getOrAdd(NIGHT_SQUID).total
            val nSquidGain = nightSquidNow - nightSquidStart

            val line = buildString {
                append("$CYAN${BOLD}Night Squids: $YELLOW${nSquidGain} $CYAN($LIGHT_GREEN${nightSquidNow}$CYAN)")
            }
            lines.add(line)

        }

        var eta: String = ""
        var inkGoal = InkFishing.goalInk

        if(items.contains(InkTrackingType.INK_GOAL)) {

            var percentage: Double = 0.0

            if(inkRate > 0 && inkGoal > totalInk) {
                val diff = inkGoal - totalInk
                val hoursNeeded = diff/inkRate
                val seconds = hoursNeeded * 3600
                eta = seconds.seconds.toReadableString()

                val ratio = totalInk.toDouble()/ inkGoal.toDouble()
                percentage = ratio*100
            }

            val line = buildString {
                append("$CYAN${BOLD}Ink Goal: $YELLOW${truncInk(inkGoal.toLong())}")
                if(inkRate > 0 && inkGoal > totalInk) append(" $CYAN($YELLOW${"%.1f".format(percentage)}%$CYAN)")
                else if(inkRate > 0) append(" $CYAN(${YELLOW}100%$CYAN)")
            }
            lines.add(line)

        }

        if(items.contains(InkTrackingType.ETA)) {

            if(inkRate > 0 && inkGoal > totalInk) {
                lines.add("${CYAN}${BOLD}ETA: $YELLOW${eta}")
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


    private fun getOverallScRate(time: Duration): Int {
        val hoursElapsed = time.inWholeMilliseconds / 3600000.0
        return if (hoursElapsed > 0) (SeaCreatureHour.total / hoursElapsed).toInt() else 0
    }

    private fun getOverallXpRate(time: Duration): Long {
        val hoursElapsed = time.inWholeMilliseconds / 3600000.0
        return if (hoursElapsed > 0) (FishingXpTracker.totalXp / hoursElapsed).toLong() else 0L
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
