package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.fishing.RareScDisplayDataType
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.fishing.SeaCreatureAvailability
import cloud.glitchdev.rfu.utils.dsl.isWearingTrophyHunterArmor

@HudElement
object RareSCDisplay : AbstractFishingHudElement("rareSCDisplay") {
    override val displaysWhilePaused: Boolean = true
    override val requiresFishing: Boolean
        get() = SeaCreatureConfig.rareScOnlyWhenFishing

    override val requirement: Boolean
        get() = !isWearingTrophyHunterArmor() && SeaCreatureConfig.rareScDisplay

    override val isElementActive: Boolean
        get() = !requiresFishing || FishingSession.pausedDuration < 1.minutes

    override fun onInitialize() {
        super.onInitialize()
        registerLocationEvent {
            CatchTracker.catchHistory.lastHotspot = null
            CatchTracker.catchHistory.lastPos = Vec3.ZERO
            CatchTracker.catchHistory.lastBait = null
            CatchTracker.catchHistory.lastLiquid = null
            updateState()
        }

        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            updateState()
        }

        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val lines = mutableListOf<String>()
        val selectedScs = SeaCreatures.entries.filter { it.special }
        val context = SeaCreatureAvailability.getCurrentContext()

        if ((context == null || context.pos == Vec3.ZERO) && !FishingSession.isFishing && !isEditing) {
            text.setText("")
            return
        }

        val dataOrder = SeaCreatureConfig.rareScDisplayDataOrder
        val catchHistory = CatchTracker.catchHistory

        selectedScs.groupBy { it.category }.forEach { (_, scsInCategory) ->
            scsInCategory.forEach { sc ->
                if (context == null || !SeaCreatureAvailability.isAvailable(sc, context)) {
                    return@forEach
                }

                val record = catchHistory.getOrAdd(sc)

                // Building the customized line
                val line = buildString {
                    val color = sc.scDisplayColor.ifEmpty { WHITE }
                    append("$color${BOLD}${sc.scDisplayName}:")

                    dataOrder.forEach { dataType ->
                        when (dataType) {
                            RareScDisplayDataType.STREAK -> {
                                append(" $YELLOW${record.count}")
                            }
                            RareScDisplayDataType.AVERAGE -> {
                                val avg = if (record.history.isNotEmpty()) ceil(record.history.average()).toInt().toString() else "0"
                                append(" $GRAY($YELLOW$avg$GRAY)")
                            }
                            RareScDisplayDataType.TOTAL -> {
                                append(" $color[$YELLOW${record.total}$color]")
                            }
                            RareScDisplayDataType.TIME_SINCE -> {
                                val lastTime = if (record.total > 0) {
                                    (Clock.System.now() - record.time).toReadableString()
                                } else {
                                    "Never"
                                }
                                append(" $WHITE$lastTime")
                            }
                        }
                    }
                }
                lines.add(line)
            }
        }

        if (isEditing && lines.isEmpty()) {
            val examples = SeaCreatures.entries.filter { it.special }.take(3)
            examples.groupBy { it.category }.forEach { (_, scsInCategory) ->
                scsInCategory.forEach { sc ->
                    val record = catchHistory.getOrAdd(sc)
                    val line = buildString {
                        val color = sc.scDisplayColor
                        append("$color${BOLD}${sc.scDisplayName}:")
                        dataOrder.forEach { dataType ->
                            when (dataType) {
                                RareScDisplayDataType.STREAK -> {
                                    append(" $YELLOW${record.count}")
                                }
                                RareScDisplayDataType.AVERAGE -> {
                                    val avg = if (record.history.isNotEmpty()) ceil(record.history.average()).toInt().toString() else "0"
                                    append(" $GRAY($YELLOW$avg$GRAY)")
                                }
                                RareScDisplayDataType.TOTAL -> {
                                    append(" $color[$YELLOW${record.total}$color]")
                                }
                                RareScDisplayDataType.TIME_SINCE -> {
                                    val lastTime = if (record.total > 0) {
                                        (Clock.System.now() - record.time).toReadableString()
                                    } else {
                                        "Never"
                                    }
                                    append(" $WHITE$lastTime")
                                }
                            }
                        }
                    }
                    lines.add(line)
                }
            }
        }

        text.setText(if (lines.isEmpty()) {
            if (isEditing) "rareSCDisplay" else ""
        } else lines.joinToString("\n"))
    }
}
