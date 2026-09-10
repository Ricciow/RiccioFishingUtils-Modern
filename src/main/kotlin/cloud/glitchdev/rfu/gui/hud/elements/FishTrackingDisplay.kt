package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.CYAN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.fishing.FishTrackingType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Duration
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.fishing.FishingSession.resetSession
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import kotlin.time.Duration.Companion.minutes

@HudElement
object FishTrackingDisplay : AbstractFishingHudElement("fishTrackingDisplay") {
    override val displaysWhilePaused: Boolean = true
    override val requiresFishing: Boolean
        get() = GeneralFishing.fishTrackingOnlyWhenFishing
    override val requirement: Boolean
        get() = GeneralFishing.fishTrackingDisplay
    override val isElementActive: Boolean
        get() = !requiresFishing || FishingSession.pausedDuration < (1.minutes + GeneralFishing.fishingTime.minutes)
    override val renderOnInventory: Boolean = true

    private lateinit var resetButton : UIText

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    init {
        create()
    }

    fun create() {
        resetButton = UIText("$LIGHT_RED[Reset]").constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = ScaledTextConstraint(scale * 1.1f)
            height = TextAspectConstraint()
        } childOf container

        resetButton.onMouseClick {
            resetSession()
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

        resetButton.setHidden(!isOnInventory)
        resetButton.constrain {
            width = ScaledTextConstraint(scale * 1.1f)
        }
    }

    private fun formatXp(value: Long): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)

            value >= 1_000 -> "%.1fk".format(value / 1_000.0)
            else -> value.toString()
        }
    }
}
