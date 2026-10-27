package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.TrophyType
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.data.fishing.TrophyPityEntry
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
object TrophyPityTracker : RegisteredEvent {
    private val goldRegex = """Progress to GOLD:\s*(\d+)/""".toRegex(RegexOption.IGNORE_CASE)
    private val diamondRegex = """Progress to DIAMOND:\s*(\d+)/""".toRegex(RegexOption.IGNORE_CASE)

    val lastCaughtTimes = mutableMapOf<String, Long>()
    private val sessionTrophyCatches = mutableMapOf<TrophyType, Int>()

    override fun register() {
        registerTrophyFrogCatchEvent { frog, tier, amount ->
            handleCatch(TrophyDataManager.data.pity.frogPity, frog.name, frog, TrophyType.FROG, tier, amount)
        }

        registerTrophyFishCatchEvent { fish, tier, amount ->
            handleCatch(TrophyDataManager.data.pity.fishPity, fish.name, fish, TrophyType.FISH, tier, amount)
        }

        registerContainerOpenEvent { _, _, items ->
            var updated = false
            val data = TrophyDataManager.data.pity
            for (item in items) {
                if (item.isEmpty) continue

                val lore = item[DataComponents.LORE] ?: continue
                var goldProgress = 0
                var diamondProgress = 0
                var found = false

                for (line in lore.lines) {
                    val plainLine = line.toUnformattedString()
                    val goldMatch = goldRegex.find(plainLine)
                    if (goldMatch != null) {
                        goldProgress = goldMatch.groupValues[1].toIntOrNull() ?: 0
                        found = true
                    }
                    val diamondMatch = diamondRegex.find(plainLine)
                    if (diamondMatch != null) {
                        diamondProgress = diamondMatch.groupValues[1].toIntOrNull() ?: 0
                        found = true
                    }
                }

                if (found) {
                    val itemName = item.hoverName.toUnformattedString()
                    val fish = TrophyFish.fromName(itemName)
                    if (fish != null) {
                        if (!data.hasSyncedFish) {
                            data.hasSyncedFish = true
                            updated = true
                        }
                        if (updatePity(TrophyDataManager.data.pity.fishPity, fish.name, fish.displayName, goldProgress, diamondProgress)) {
                            updated = true
                        }
                    } else {
                        val frog = TrophyFrog.fromName(itemName)
                        if (frog != null) {
                            if (!data.hasSyncedFrogs) {
                                data.hasSyncedFrogs = true
                                updated = true
                            }
                            if (updatePity(TrophyDataManager.data.pity.frogPity, frog.name, frog.displayName, goldProgress, diamondProgress)) {
                                updated = true
                            }
                        }
                    }
                }
            }

            if (updated) {
                TrophyDataManager.save()
            }
        }
    }

    private fun handleCatch(
        pityMap: MutableMap<String, TrophyPityEntry>,
        key: String,
        trophy: Trophy,
        type: TrophyType,
        tier: TrophyTier,
        amount: Int = 1
    ) {
        val existing = pityMap[key] ?: TrophyPityEntry(trophy.displayName, 0, 0)
        lastCaughtTimes[key] = System.currentTimeMillis()
        incrementPity(existing, trophy, tier, amount)
        pityMap[key] = existing
        TrophyDataManager.save()

        val catches = sessionTrophyCatches.getOrDefault(type, 0) + amount
        sessionTrophyCatches[type] = catches
        val data = TrophyDataManager.data.pity
        val hasSynced = when (type) {
            TrophyType.FROG -> data.hasSyncedFrogs
            TrophyType.FISH -> data.hasSyncedFish
        }
        if (!hasSynced && catches <= 10) {
            sendSyncAlert(type)
        }
    }

    private fun sendSyncAlert(type: TrophyType) {
        val tab = if (type == TrophyType.FROG) "Lotus Atoll" else "Crimson Isle"
        val message = TextUtils.rfuLiteral(
            "Run /pity and open the $tab tab to sync your pity data.",
            TextColor.YELLOW
        )
        message.append(IgnoreTrophySyncAlertCommand.button("pity", type))
        Chat.sendMessage(message)
    }

    private fun incrementPity(existing: TrophyPityEntry, trophy: Trophy, tier: TrophyTier, amount: Int = 1) {
        when (tier) {
            TrophyTier.BRONZE, TrophyTier.SILVER -> {
                existing.goldProgress = minOf(existing.goldProgress + amount, trophy.goldPity)
                existing.diamondProgress = minOf(existing.diamondProgress + amount, trophy.diamondPity)
            }
            TrophyTier.GOLD -> {
                existing.goldProgress = 0
                existing.diamondProgress = minOf(existing.diamondProgress + amount, trophy.diamondPity)
            }
            TrophyTier.DIAMOND -> {
                existing.goldProgress = minOf(existing.goldProgress + amount, trophy.goldPity)
                existing.diamondProgress = 0
            }
        }
    }

    private fun updatePity(
        pityMap: MutableMap<String, TrophyPityEntry>,
        key: String,
        displayName: String,
        goldProgress: Int,
        diamondProgress: Int
    ): Boolean {
        val existing = pityMap[key]
        if (existing == null ||
            existing.goldProgress != goldProgress ||
            existing.diamondProgress != diamondProgress
        ) {
            pityMap[key] = TrophyPityEntry(
                name = displayName,
                goldProgress = goldProgress,
                diamondProgress = diamondProgress
            )
            return true
        }
        return false
    }

}
