package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.TrophyType
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.fishing.TrophyCountEntry
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@AutoRegister
object TrophyCountTracker : RegisteredEvent {
    private val countRegex = """^(Bronze|Silver|Gold|Diamond)\s+.(?:\s*\(([\d,]+)\))?$"""
        .toRegex(RegexOption.IGNORE_CASE)
    private val syncAlertsSent = mutableSetOf<TrophyType>()

    override fun register() {
        registerTrophyFrogCatchEvent { frog, tier, amount ->
            TrophyDataManager.data.count.frogCount.getOrPut(frog.name) { TrophyCountEntry(frog.displayName) }
                .addCatch(tier, amount)
            TrophyDataManager.save()
            sendSyncAlert(TrophyType.FROG)
        }

        registerTrophyFishCatchEvent { fish, tier, amount ->
            TrophyDataManager.data.count.fishCount.getOrPut(fish.name) { TrophyCountEntry(fish.displayName) }
                .addCatch(tier, amount)
            TrophyDataManager.save()
            sendSyncAlert(TrophyType.FISH)
        }

        registerContainerOpenEvent { containerName, _, items ->
            if (containerName != "Trophy Frogs" && containerName != "Trophy Fish") return@registerContainerOpenEvent
            val data = TrophyDataManager.data.count
            var updated = false
            for (item in items) {
                if (item.isEmpty) continue
                val itemName = item.hoverName.toUnformattedString()
                val trophy = if (containerName == "Trophy Frogs") TrophyFrog.fromName(itemName)
                    else TrophyFish.fromName(itemName)
                if (trophy == null) continue
                val lore = item[DataComponents.LORE] ?: continue
                val entry = parseCounts(trophy.displayName, lore.lines.map { it.toUnformattedString() }) ?: continue
                val counts = if (containerName == "Trophy Frogs") data.frogCount else data.fishCount
                if (counts[trophy.name] != entry) {
                    counts[trophy.name] = entry
                    updated = true
                }
                if (containerName == "Trophy Frogs" && !data.hasSyncedFrogs) {
                    data.hasSyncedFrogs = true
                    updated = true
                }
                if (containerName == "Trophy Fish" && !data.hasSyncedFish) {
                    data.hasSyncedFish = true
                    updated = true
                }
            }
            if (updated) {
                TrophyDataManager.save()
            }
        }
    }

    private fun sendSyncAlert(type: TrophyType) {
        val data = TrophyDataManager.data.count
        val hasSynced = when (type) {
            TrophyType.FROG -> data.hasSyncedFrogs
            TrophyType.FISH -> data.hasSyncedFish
        }
        if (hasSynced || !syncAlertsSent.add(type)) return
        val npc = if (type == TrophyType.FROG) "Ribery" else "Odger"
        Chat.sendMessage(TextUtils.rfuLiteral(
            "Talk to $npc to sync your total trophy counts. ",
            TextColor.YELLOW
        ).append(IgnoreTrophySyncAlertCommand.button("count", type)))
    }

    internal fun parseCounts(name: String, lines: List<String>): TrophyCountEntry? {
        val counts = mutableMapOf<TrophyTier, Int>()
        for (line in lines) {
            val match = countRegex.matchEntire(line.trim()) ?: continue
            val tier = TrophyTier.valueOf(match.groupValues[1].uppercase())
            val amount = if (match.groupValues[2].isEmpty()) 0
                else match.groupValues[2].replace(",", "").toIntOrNull() ?: return null
            counts[tier] = amount
        }

        if (counts.size != TrophyTier.entries.size) return null
        return TrophyCountEntry(
            name = name,
            bronze = counts.getValue(TrophyTier.BRONZE),
            silver = counts.getValue(TrophyTier.SILVER),
            gold = counts.getValue(TrophyTier.GOLD),
            diamond = counts.getValue(TrophyTier.DIAMOND)
        )
    }
}
