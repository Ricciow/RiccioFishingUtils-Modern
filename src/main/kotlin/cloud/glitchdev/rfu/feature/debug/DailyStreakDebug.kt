package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import java.util.Random

object DailyStreakDebug : AbstractCommand("dailies") {
    override val description: String = "Debug commands for daily streaks and challenges."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            lit("reset").executes {
                DailyStreakManager.data.currentDate = ""
                DailyStreakManager.data.todayChallenges = emptyList()
                DailyStreakManager.checkDailyReset()
                Chat.sendMessage(Component.literal("§b[RFU Debug] §aDaily challenges reset for today!"))
                1
            }
        )

        builder.then(
            lit("resetreroll").executes {
                DailyStreakManager.data.hasRerolledToday = false
                DailyStreakManager.saveData()
                Chat.sendMessage(Component.literal("§b[RFU Debug] §aDaily reroll reset! You can reroll again today."))
                1
            }
        )

        builder.then(
            lit("complete").then(
                lit("all").executes {
                    DailyStreakManager.data.todayChallenges.forEach { challenge ->
                        if (!challenge.isCompleted) {
                            val needed = challenge.getTargetProgress() - challenge.currentProgress
                            if (needed > 0) {
                                DailyStreakManager.addProgressForChallenge(challenge.id, needed)
                            }
                        }
                    }
                    Chat.sendMessage(Component.literal("§b[RFU Debug] §aAll today's challenges completed!"))
                    1
                }
            ).then(
                arg("index", IntegerArgumentType.integer(1, 3)).executes { context ->
                    val index = IntegerArgumentType.getInteger(context, "index") - 1
                    val challenge = DailyStreakManager.data.todayChallenges.getOrNull(index)
                    if (challenge != null) {
                        val needed = challenge.getTargetProgress() - challenge.currentProgress
                        if (needed > 0) {
                            DailyStreakManager.addProgressForChallenge(challenge.id, needed)
                        }
                        Chat.sendMessage(Component.literal("§b[RFU Debug] §aCompleted challenge #${index + 1}: ${challenge.getTitle()}"))
                    } else {
                        Chat.sendMessage(Component.literal("§cInvalid challenge index!"))
                    }
                    1
                }
            )
        )

        builder.then(
            lit("progress").then(
                arg("index", IntegerArgumentType.integer(1, 3)).then(
                    arg("amount", IntegerArgumentType.integer(1)).executes { context ->
                        val index = IntegerArgumentType.getInteger(context, "index") - 1
                        val amount = IntegerArgumentType.getInteger(context, "amount")
                        val challenge = DailyStreakManager.data.todayChallenges.getOrNull(index)
                        if (challenge != null) {
                            DailyStreakManager.addProgressForChallenge(challenge.id, amount)
                            Chat.sendMessage(Component.literal("§b[RFU Debug] §aAdded $amount progress to #${index + 1}: ${challenge.getTitle()}"))
                        } else {
                            Chat.sendMessage(Component.literal("§cInvalid challenge index!"))
                        }
                        1
                    }
                )
            )
        )

        builder.then(
            lit("setstreak").then(
                arg("amount", IntegerArgumentType.integer(0)).executes { context ->
                    val amount = IntegerArgumentType.getInteger(context, "amount")
                    DailyStreakManager.data.currentStreak = amount
                    if (amount > DailyStreakManager.data.highestStreak) {
                        DailyStreakManager.data.highestStreak = amount
                    }
                    DailyStreakManager.saveData()
                    Chat.sendMessage(Component.literal("§b[RFU Debug] §aDaily streak set to $amount days!"))
                    1
                }
            )
        )

        builder.then(
            lit("clear").executes {
                DailyStreakManager.unregisterAllListeners()
                DailyStreakManager.data.currentStreak = 0
                DailyStreakManager.data.highestStreak = 0
                DailyStreakManager.data.totalChallengesCompleted = 0
                DailyStreakManager.data.totalDaysCompleted = 0
                DailyStreakManager.data.lastCompletedDate = ""
                DailyStreakManager.data.currentDate = ""
                DailyStreakManager.data.hasRerolledToday = false
                DailyStreakManager.data.todayChallenges = emptyList()
                DailyStreakManager.saveData()
                Chat.sendMessage(Component.literal("§b[RFU Debug] §cCleared all daily streak data!"))
                1
            }
        )

        val generateAction = { days: Int ->
            generateDailyChallenges(days)
            1
        }

        builder.then(
            lit("generate")
                .executes { generateAction(30) }
                .then(
                    arg("days", IntegerArgumentType.integer(1, 10000))
                        .executes { context ->
                            generateAction(IntegerArgumentType.getInteger(context, "days"))
                        }
                )
        )

        builder.then(
            lit("gen")
                .executes { generateAction(30) }
                .then(
                    arg("days", IntegerArgumentType.integer(1, 10000))
                        .executes { context ->
                            generateAction(IntegerArgumentType.getInteger(context, "days"))
                        }
                )
        )

        val simulateAction = { runs: Int ->
            simulateDailyChallenges(runs)
            1
        }

        builder.then(
            lit("simulate")
                .executes { simulateAction(1000) }
                .then(
                    arg("runs", IntegerArgumentType.integer(1, 100000))
                        .executes { context ->
                            simulateAction(IntegerArgumentType.getInteger(context, "runs"))
                        }
                )
        )

        builder.then(
            lit("sim")
                .executes { simulateAction(1000) }
                .then(
                    arg("runs", IntegerArgumentType.integer(1, 100000))
                        .executes { context ->
                            simulateAction(IntegerArgumentType.getInteger(context, "runs"))
                        }
                )
        )

        val poolAction = {
            listChallengePool()
            1
        }

        builder.then(lit("pool").executes { poolAction() })
        builder.then(lit("list").executes { poolAction() })
    }

    private data class ChallengeStat(
        val challenge: BaseChallenge,
        var appearances: Int = 0,
        var dayRate: Double = 0.0,
        var slotShare: Double = 0.0,
        var basePoolWeightPct: Double = 0.0
    )

    private fun generateDailyChallenges(days: Int) {
        val pool = ChallengeRegistry.getPoolChallenges()
        if (pool.isEmpty()) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §cNo challenges found in ChallengeRegistry!"))
            return
        }

        val totalPoolWeight = pool.sumOf { it.weight.coerceAtLeast(1) }
        val username = try { User.getUsername() } catch (_: Exception) { "" }
        val startDate = LocalDate.now(ZoneOffset.UTC)

        val dailySchedule = mutableListOf<Pair<String, List<BaseChallenge>>>()
        val countsMap = mutableMapOf<String, Int>()

        for (dayIndex in 0 until days) {
            val dateStr = startDate.plusDays(dayIndex.toLong()).toString()
            val seed = "$dateStr:$username".hashCode().toLong()
            val selected = ChallengeRegistry.getSeededPoolChallenges(seed, 3)

            dailySchedule.add(dateStr to selected)
            selected.forEach { challenge ->
                countsMap[challenge.id] = (countsMap[challenge.id] ?: 0) + 1
            }
        }

        val stats = pool.map { challenge ->
            val count = countsMap[challenge.id] ?: 0
            val dayRate = count.toDouble() / days * 100.0
            val slotShare = count.toDouble() / (days * 3) * 100.0
            val basePoolPct = challenge.weight.coerceAtLeast(1).toDouble() / totalPoolWeight * 100.0
            ChallengeStat(challenge, count, dayRate, slotShare, basePoolPct)
        }.sortedWith(compareByDescending<ChallengeStat> { it.appearances }.thenByDescending { it.challenge.weight })

        // Chat Output
        Chat.sendMessage(Component.literal("§b[RFU Debug] §6=== Generated Tasks for $days Days ==="))

        if (days <= 10) {
            dailySchedule.forEachIndexed { index, (dateStr, challenges) ->
                val titles = challenges.joinToString("§7, §e") { it.getTitle(0) }
                Chat.sendMessage(Component.literal("§7Day ${index + 1} ($dateStr): §e$titles"))
            }
        } else {
            dailySchedule.take(5).forEachIndexed { index, (dateStr, challenges) ->
                val titles = challenges.joinToString("§7, §e") { it.getTitle(0) }
                Chat.sendMessage(Component.literal("§7Day ${index + 1} ($dateStr): §e$titles"))
            }
            Chat.sendMessage(Component.literal("§7... and ${days - 5} more days (see clipboard)"))
        }

        val uniquePicked = stats.count { it.appearances > 0 }
        Chat.sendMessage(Component.literal("§b[RFU Debug] §7Pool: §e${pool.size} challenges §7| Picked: §a$uniquePicked/${pool.size} unique §7| Total Slots: §e${days * 3}"))

        val top3 = stats.take(3).filter { it.appearances > 0 }
        if (top3.isNotEmpty()) {
            val topStr = top3.joinToString("§7, §f") {
                "${it.challenge.getTitle(0)} §a(${it.appearances}x / ${String.format(Locale.US, "%.1f", it.dayRate)}%)"
            }
            Chat.sendMessage(Component.literal("§b[RFU Debug] §aTop Picked: §f$topStr"))
        }

        val neverPicked = stats.filter { it.appearances == 0 }
        if (neverPicked.isNotEmpty()) {
            val unpickedStr = neverPicked.take(5).joinToString("§7, §c") { it.challenge.getTitle(0) }
            val extra = if (neverPicked.size > 5) " §7(+${neverPicked.size - 5} more)" else ""
            Chat.sendMessage(Component.literal("§b[RFU Debug] §cNever Picked (${neverPicked.size}): §c$unpickedStr$extra"))
        }

        // Generate full markdown report for clipboard
        val report = buildString {
            appendLine("# Daily Challenges Generation Report ($days Days)")
            appendLine("- **Username Seed:** ${if (username.isNotEmpty()) username else "<none>"}")
            appendLine("- **Start Date:** ${startDate}")
            appendLine("- **End Date:** ${startDate.plusDays(days.toLong() - 1)}")
            appendLine("- **Total Days Simulated:** $days")
            appendLine("- **Total Task Slots:** ${days * 3}")
            appendLine("- **Pool Size:** ${pool.size} challenges")
            appendLine("- **Total Pool Weight:** $totalPoolWeight")
            appendLine("- **Unique Challenges Selected:** $uniquePicked / ${pool.size} (${String.format(Locale.US, "%.1f", uniquePicked.toDouble() / pool.size * 100.0)}%)")
            appendLine()
            appendLine("## Daily Task Schedule")
            dailySchedule.forEachIndexed { index, (dateStr, challenges) ->
                appendLine("### Day ${index + 1} ($dateStr)")
                challenges.forEachIndexed { cIndex, c ->
                    appendLine("${cIndex + 1}. **${c.getTitle(0)}** (`${c.id}`) - Target: ${c.getTargetProgress(0)} (Streak 0) - Weight: ${c.weight}")
                    appendLine("   - *Description:* ${c.getDescription(0)}")
                }
                appendLine()
            }
            appendLine("## Challenge Distribution Table")
            appendLine("| Rank | ID | Title | Weight | Appearances | Day Rate % | Slot Share % | Base Pool % |")
            appendLine("| ---: | :--- | :--- | ---: | ---: | ---: | ---: | ---: |")
            stats.forEachIndexed { rank, stat ->
                appendLine("| ${rank + 1} | `${stat.challenge.id}` | ${stat.challenge.getTitle(0)} | ${stat.challenge.weight} | ${stat.appearances} | ${String.format(Locale.US, "%.2f", stat.dayRate)}% | ${String.format(Locale.US, "%.2f", stat.slotShare)}% | ${String.format(Locale.US, "%.2f", stat.basePoolWeightPct)}% |")
            }
        }

        try {
            mc.keyboardHandler.clipboard = report
            Chat.sendMessage(Component.literal("§b[RFU Debug] §aFull task list and distribution report copied to clipboard!"))
        } catch (_: Exception) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §eCould not copy report to clipboard."))
        }
    }

    private fun simulateDailyChallenges(runs: Int) {
        val pool = ChallengeRegistry.getPoolChallenges()
        if (pool.isEmpty()) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §cNo challenges found in ChallengeRegistry!"))
            return
        }

        val totalPoolWeight = pool.sumOf { it.weight.coerceAtLeast(1) }
        val countsMap = mutableMapOf<String, Int>()
        val rng = Random()

        for (run in 0 until runs) {
            val candidatePool = pool.toMutableList()
            repeat(3.coerceAtMost(candidatePool.size)) {
                val picked = ChallengeRegistry.getWeightedRandomChallenge(candidatePool, rng) ?: return@repeat
                countsMap[picked.id] = (countsMap[picked.id] ?: 0) + 1
                candidatePool.remove(picked)
            }
        }

        val stats = pool.map { challenge ->
            val count = countsMap[challenge.id] ?: 0
            val dayRate = count.toDouble() / runs * 100.0
            val slotShare = count.toDouble() / (runs * 3) * 100.0
            val basePoolPct = challenge.weight.coerceAtLeast(1).toDouble() / totalPoolWeight * 100.0
            ChallengeStat(challenge, count, dayRate, slotShare, basePoolPct)
        }.sortedWith(compareByDescending<ChallengeStat> { it.appearances }.thenByDescending { it.challenge.weight })

        // Chat Output
        val uniquePicked = stats.count { it.appearances > 0 }
        Chat.sendMessage(Component.literal("§b[RFU Debug] §6=== Daily Challenge Monte Carlo Simulation ($runs Runs) ==="))
        Chat.sendMessage(Component.literal("§b[RFU Debug] §7Pool: §e${pool.size} challenges §7| Picked: §a$uniquePicked/${pool.size} unique §7| Total Slots: §e${runs * 3}"))

        val top3 = stats.take(3).filter { it.appearances > 0 }
        if (top3.isNotEmpty()) {
            val topStr = top3.joinToString("§7, §f") {
                "${it.challenge.getTitle(0)} §a(${String.format(Locale.US, "%.1f", it.dayRate)}%)"
            }
            Chat.sendMessage(Component.literal("§b[RFU Debug] §aTop 3: §f$topStr"))
        }

        val bottom3 = stats.filter { it.appearances > 0 }.takeLast(3).reversed()
        if (bottom3.isNotEmpty()) {
            val botStr = bottom3.joinToString("§7, §e") {
                "${it.challenge.getTitle(0)} §e(${String.format(Locale.US, "%.2f", it.dayRate)}%)"
            }
            Chat.sendMessage(Component.literal("§b[RFU Debug] §eBottom 3: §f$botStr"))
        }

        val neverPicked = stats.filter { it.appearances == 0 }
        if (neverPicked.isNotEmpty()) {
            val unpickedStr = neverPicked.take(5).joinToString("§7, §c") { it.challenge.getTitle(0) }
            val extra = if (neverPicked.size > 5) " §7(+${neverPicked.size - 5} more)" else ""
            Chat.sendMessage(Component.literal("§b[RFU Debug] §cNever Picked (${neverPicked.size}): §c$unpickedStr$extra"))
        }

        // Markdown Report
        val report = buildString {
            appendLine("# Daily Challenges Monte Carlo Simulation ($runs Runs)")
            appendLine("- **Simulated Days/Runs:** $runs")
            appendLine("- **Total Task Slots:** ${runs * 3}")
            appendLine("- **Pool Size:** ${pool.size} challenges")
            appendLine("- **Total Pool Weight:** $totalPoolWeight")
            appendLine("- **Unique Challenges Selected:** $uniquePicked / ${pool.size} (${String.format(Locale.US, "%.1f", uniquePicked.toDouble() / pool.size * 100.0)}%)")
            appendLine()
            appendLine("## Challenge Distribution Table")
            appendLine("| Rank | ID | Title | Weight | Appearances | Day Rate % | Slot Share % | Base Pool % |")
            appendLine("| ---: | :--- | :--- | ---: | ---: | ---: | ---: | ---: |")
            stats.forEachIndexed { rank, stat ->
                appendLine("| ${rank + 1} | `${stat.challenge.id}` | ${stat.challenge.getTitle(0)} | ${stat.challenge.weight} | ${stat.appearances} | ${String.format(Locale.US, "%.2f", stat.dayRate)}% | ${String.format(Locale.US, "%.2f", stat.slotShare)}% | ${String.format(Locale.US, "%.2f", stat.basePoolWeightPct)}% |")
            }
        }

        try {
            mc.keyboardHandler.clipboard = report
            Chat.sendMessage(Component.literal("§b[RFU Debug] §aFull simulation report copied to clipboard!"))
        } catch (_: Exception) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §eCould not copy report to clipboard."))
        }
    }

    private fun listChallengePool() {
        val pool = ChallengeRegistry.getPoolChallenges()
        if (pool.isEmpty()) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §cNo challenges found in ChallengeRegistry!"))
            return
        }

        val totalPoolWeight = pool.sumOf { it.weight.coerceAtLeast(1) }
        val sorted = pool.sortedByDescending { it.weight.coerceAtLeast(1) }

        Chat.sendMessage(Component.literal("§b[RFU Debug] §6=== Registered Challenge Pool (${pool.size} Challenges) ==="))
        Chat.sendMessage(Component.literal("§b[RFU Debug] §7Total Pool Weight: §e$totalPoolWeight"))

        val report = buildString {
            appendLine("# Registered Daily Challenges Pool (${pool.size} Challenges)")
            appendLine("- **Total Pool Weight:** $totalPoolWeight")
            appendLine()
            appendLine("| ID | Title | Description | Target (Streak 0) | Weight | Base Pool % |")
            appendLine("| :--- | :--- | :--- | ---: | ---: | ---: |")
            sorted.forEach { c ->
                val weightPct = c.weight.coerceAtLeast(1).toDouble() / totalPoolWeight * 100.0
                appendLine("| `${c.id}` | ${c.getTitle(0)} | ${c.getDescription(0)} | ${c.getTargetProgress(0)} | ${c.weight} | ${String.format(Locale.US, "%.2f", weightPct)}% |")
            }
        }

        try {
            mc.keyboardHandler.clipboard = report
            Chat.sendMessage(Component.literal("§b[RFU Debug] §aFull pool list copied to clipboard!"))
        } catch (_: Exception) {
            Chat.sendMessage(Component.literal("§b[RFU Debug] §eCould not copy pool list to clipboard."))
        }
    }
}

