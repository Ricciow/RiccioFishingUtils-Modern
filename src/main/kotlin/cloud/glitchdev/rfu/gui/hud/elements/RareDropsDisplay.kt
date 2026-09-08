package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.DropsSettings
import cloud.glitchdev.rfu.constants.fishing.IRareDrop
import cloud.glitchdev.rfu.constants.fishing.RareDropDisplayDataType
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.constants.text.TextEffects.RESET
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.drops.DropHistory
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.gui.components.elementa.MultilineHudText
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.isWearingTrophyHunterArmor
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.utils.fishing.SeaCreatureAvailability
import java.awt.Color
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@HudElement
object RareDropsDisplay : AbstractFishingHudElement("rareDropsDisplay") {
    override val displaysWhilePaused: Boolean = true
    override val requiresFishing: Boolean
        get() = DropsSettings.rareDropsOnlyWhenFishing

    override val requirement: Boolean
        get() = !isWearingTrophyHunterArmor() && DropsSettings.rareDropsDisplay

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

        DropEvents.registerRareDropEvent { _, _, _ ->
            updateState()
            true
        }

        DropEvents.registerDyeDropEvent { _, _, _ ->
            updateState()
        }

        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    private fun formatDropLine(
        drop: IRareDrop,
        dataOrder: Array<out RareDropDisplayDataType>
    ): MultilineHudText.HudLine {
        val entry: DropHistory.IDropEntry? = when (drop) {
            is RareDrops -> DropManager.dropHistory.getOrAdd(drop)
            is Dyes -> DropManager.dropHistory.getOrAdd(drop)
            else -> null
        }

        val currentTotal = drop.relatedScs.sumOf { sc -> CatchTracker.catchHistory.getOrAdd(sc).total }
        val lastDrop = entry?.history?.lastOrNull()
        val streak = if (lastDrop == null) {
            currentTotal
        } else {
            (currentTotal - lastDrop.totalCount).coerceAtLeast(0)
        }
        val sinceCounts = entry?.history?.mapNotNull { it.sinceCount } ?: emptyList()
        val total = entry?.history?.size ?: 0

        val isDye = drop is Dyes
        val lineColor = if (isDye) {
            drop.hex.toIntOrNull(16)?.let { Color(it) } ?: Color.WHITE
        } else {
            Color.WHITE
        }

        val lineText = buildString {
            if (isDye) {
                append("${BOLD}${drop.displayName}:")
            } else {
                append("${drop.rarity.color.code}${BOLD}${drop.displayName}:")
            }

            dataOrder.forEach { dataType ->
                when (dataType) {
                    RareDropDisplayDataType.STREAK -> {
                        append(" $YELLOW$streak")
                    }
                    RareDropDisplayDataType.AVERAGE -> {
                        val avg = if (sinceCounts.isNotEmpty()) ceil(sinceCounts.average()).toInt().toString() else "0"
                        append(" $GRAY($YELLOW$avg$GRAY)")
                    }
                    RareDropDisplayDataType.TOTAL -> {
                        if (isDye) {
                            append(" $RESET[$YELLOW$total$RESET]")
                        } else {
                            val color = drop.rarity.color.code
                            append(" $color[$YELLOW$total$color]")
                        }
                    }
                    RareDropDisplayDataType.TIME_SINCE -> {
                        val lastTime = if (lastDrop != null) {
                            (Clock.System.now() - lastDrop.date).toReadableString()
                        } else {
                            "Never"
                        }
                        append(" $WHITE$lastTime")
                    }
                }
            }
        }

        return MultilineHudText.HudLine(lineText, lineColor)
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val lines = mutableListOf<MultilineHudText.HudLine>()
        val context = SeaCreatureAvailability.getCurrentContext()

        if ((context == null || context.pos == Vec3.ZERO) && !FishingSession.isFishing && !isEditing) {
            text.setText("")
            return
        }

        val availableCreatures = if (context != null) {
            SeaCreatureAvailability.getAvailableCreatures(context = context).toSet()
        } else {
            emptySet()
        }

        val dataOrder = DropsSettings.rareDropsDisplayDataOrder

        val availableDyes = if (DropsSettings.rareDropsDisplayDyes) {
            DropsSettings.dyeDrops
                .filter { dye -> dye.relatedScs.any { it in availableCreatures } }
                .sortedWith(compareByDescending<Dyes> { it.rarity }.thenBy { it.displayName })
        } else {
            emptyList()
        }

        val availableDrops = DropsSettings.displayedRareDrops
            .filter { drop -> drop.relatedScs.any { it in availableCreatures } }
            .sortedWith(compareByDescending<RareDrops> { it.rarity }.thenBy { it.displayName })

        val dropsToDisplay: List<IRareDrop> = availableDyes + availableDrops

        dropsToDisplay.forEach { drop ->
            lines.add(formatDropLine(drop, dataOrder))
        }

        if (isEditing && lines.isEmpty()) {
            val sampleDyes = if (DropsSettings.rareDropsDisplayDyes) {
                DropsSettings.dyeDrops
                    .sortedWith(compareByDescending<Dyes> { it.rarity }.thenBy { it.displayName })
                    .take(1)
            } else {
                emptyList()
            }

            val sampleDrops = DropsSettings.displayedRareDrops
                .sortedWith(compareByDescending<RareDrops> { it.rarity }.thenBy { it.displayName })
                .take(3 - sampleDyes.size)
                .ifEmpty {
                    RareDrops.entries
                        .sortedWith(compareByDescending<RareDrops> { it.rarity }.thenBy { it.displayName })
                        .take(3 - sampleDyes.size)
                }

            val sampleList: List<IRareDrop> = (sampleDyes + sampleDrops).ifEmpty {
                RareDrops.entries.take(3)
            }

            sampleList.forEach { drop ->
                lines.add(formatDropLine(drop, dataOrder))
            }
        }

        if (lines.isEmpty()) {
            text.setText(if (isEditing) "rareDropsDisplay" else "")
        } else {
            text.setLines(lines)
        }
    }
}
