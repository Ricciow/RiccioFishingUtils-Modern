package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.RareScDisplayDataType
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.time.Clock

import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.fishing.BaitManager
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents

@HudElement
object RareSCDisplay : AbstractTextHudElement("rareSCDisplay") {

    private val isFishing: Boolean
        get() = FishingSession.isFishing

    override val enabled: Boolean
        get() = SeaCreatureConfig.rareScDisplay && (super.enabled || !SeaCreatureConfig.rareScOnlyWhenFishing || isFishing)

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
        val currentIsland = World.island

        val catchHistory = CatchTracker.catchHistory
        var lastHotspot = catchHistory.lastHotspot
        var lastPos = catchHistory.lastPos
        var lastBait = catchHistory.lastBait
        var lastLiquid = catchHistory.lastLiquid

        val player = mc.player
        if (lastPos == Vec3.ZERO && player != null) {
            lastPos = player.position()
            lastHotspot = HotSpotEvents.getHotspotAt(lastPos)
            lastBait = BaitManager.lastBait
            lastLiquid = lastHotspot?.liquid
        }

        val bobber = player?.fishing
        if (bobber != null) {
            lastLiquid = when {
                bobber.isInWater -> LiquidTypes.WATER
                bobber.isInLava -> LiquidTypes.LAVA
                else -> lastLiquid
            }
        }

        if (lastLiquid == null && currentIsland != null && currentIsland.availableLiquids.size == 1) {
            lastLiquid = currentIsland.availableLiquids.first()
        }

        if (lastPos == Vec3.ZERO && !isFishing && !isEditing) {
            text.setText("")
            return
        }

        val dataOrder = SeaCreatureConfig.rareScDisplayDataOrder

        selectedScs.groupBy { it.category }.forEach { (_, scsInCategory) ->
            scsInCategory.forEach { sc ->
                if (currentIsland == null || !sc.category.islands.contains(currentIsland)) {
                    return@forEach
                }

                if (lastPos != Vec3.ZERO && !sc.condition(lastHotspot, lastPos, lastBait)) {
                    return@forEach
                }

                if (lastLiquid != null && sc.liquidType != lastLiquid) {
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
