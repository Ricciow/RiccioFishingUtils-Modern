package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

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
    }
}
