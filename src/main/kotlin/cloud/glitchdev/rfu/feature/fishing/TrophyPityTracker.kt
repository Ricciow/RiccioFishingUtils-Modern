package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.constants.fishing.Trophy
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.data.fishing.TrophyPityEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component

@AutoRegister
object TrophyPityTracker : RegisteredEvent {
    private val goldRegex = """Progress to GOLD:\s*(\d+)/""".toRegex(RegexOption.IGNORE_CASE)
    private val diamondRegex = """Progress to DIAMOND:\s*(\d+)/""".toRegex(RegexOption.IGNORE_CASE)

    val lastCaughtTimes = mutableMapOf<String, Long>()
    var sessionTrophyCatches = 0

    override fun register() {
        registerTrophyFrogCatchEvent { frog, tier ->
            handleCatch(TrophyDataManager.data.pity.frogPity, frog.name, frog, tier)
        }

        registerTrophyFishCatchEvent { fish, tier ->
            handleCatch(TrophyDataManager.data.pity.fishPity, fish.name, fish, tier)
        }

        registerContainerOpenEvent { _, items ->
            var updated = false
            var syncedAny = false
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
                    syncedAny = true
                    val itemName = item.hoverName.toUnformattedString()
                    val fish = TrophyFish.fromName(itemName)
                    if (fish != null) {
                        if (updatePity(TrophyDataManager.data.pity.fishPity, fish.name, fish.displayName, goldProgress, diamondProgress)) {
                            updated = true
                        }
                    } else {
                        val frog = TrophyFrog.fromName(itemName)
                        if (frog != null) {
                            if (updatePity(TrophyDataManager.data.pity.frogPity, frog.name, frog.displayName, goldProgress, diamondProgress)) {
                                updated = true
                            }
                        }
                    }
                }
            }

            if (syncedAny && !TrophyDataManager.data.hasSynced) {
                TrophyDataManager.data.hasSynced = true
                updated = true
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
        tier: TrophyTier
    ) {
        val existing = pityMap[key] ?: TrophyPityEntry(trophy.displayName, 0, 0)
        lastCaughtTimes[key] = System.currentTimeMillis()
        incrementPity(existing, trophy, tier)
        pityMap[key] = existing
        TrophyDataManager.save()

        sessionTrophyCatches++
        if (!TrophyDataManager.data.hasSynced && sessionTrophyCatches <= 10) {
            sendSyncAlert()
        }
    }

    private fun sendSyncAlert() {
        val message = TextUtils.rfuLiteral("Trophy caught! Run /pity to sync your pity data. ", TextColor.GOLD)
        val runButton = Component.literal("${TextColor.LIGHT_GREEN}${TextEffects.BOLD}[Open Pity]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/pity"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to run /pity")))
            )
        val ignoreButton = Component.literal(" ${TextColor.LIGHT_RED}${TextEffects.BOLD}[Ignore]")
            .setStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("/rfuignorepityalert"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to hide this warning forever")))
            )
        message.append(runButton).append(ignoreButton)
        Chat.sendMessage(message)
    }

    private fun incrementPity(existing: TrophyPityEntry, trophy: Trophy, tier: TrophyTier) {
        when (tier) {
            TrophyTier.BRONZE, TrophyTier.SILVER -> {
                existing.goldProgress = minOf(existing.goldProgress + 1, trophy.goldPity)
                existing.diamondProgress = minOf(existing.diamondProgress + 1, trophy.diamondPity)
            }
            TrophyTier.GOLD -> {
                existing.goldProgress = 0
                existing.diamondProgress = minOf(existing.diamondProgress + 1, trophy.diamondPity)
            }
            TrophyTier.DIAMOND -> {
                existing.goldProgress = minOf(existing.goldProgress + 1, trophy.goldPity)
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

    @Command
    object IgnorePityAlertCommand : SimpleCommand("rfuignorepityalert") {
        override val description: String = "Disables the trophy pity sync alert."

        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            TrophyDataManager.data.hasSynced = true
            TrophyDataManager.save()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("Trophy pity sync alert has been disabled!", TextColor.LIGHT_GREEN)
            )
            return 1
        }
    }
}
