package cloud.glitchdev.rfu.feature.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.gui.window.DailyStreakWindow
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import com.mojang.brigadier.arguments.IntegerArgumentType

@Command
object DailyStreakCommand : AbstractCommand("rfudailies") {
    override val description: String = "Opens the daily streaks and challenges window."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            if(DailyStreakSettings.dailyStreakEnabled) {
                DailyStreakManager.checkDailyReset()
                Gui.openGui(DailyStreakWindow)
            } else {
                Chat.sendMessage(TextUtils.rfuLiteral("${LIGHT_RED}Daily Streaks are disabled!"))
            }
            1
        }

        builder.then(
            lit("check").executes {
                if(DailyStreakSettings.dailyStreakEnabled) {
                    DailyStreakManager.checkDailyReset()
                    val data = DailyStreakManager.data
                    Chat.sendMessage(TextUtils.rfuLiteral("$WHITE\uE11F$GOLD Daily Streak: $YELLOW{data.currentStreak} Days $GRAY(Highest: ${data.highestStreak})"))
                    data.todayChallenges.forEachIndexed { index, c ->
                        val status = if (c.isCompleted) "${LIGHT_GREEN}✔" else "$LIGHT_RED${c.currentProgress}/${c.getTargetProgress()}"
                        Chat.sendMessage(TextUtils.rfuLiteral("  $GRAY[${index + 1}] $YELLOW{c.getTitle()}$GRAY: ${c.getDescription()} - $status"))
                    }
                } else {
                    Chat.sendMessage(TextUtils.rfuLiteral("${LIGHT_RED}Daily Streaks are disabled!"))
                }
                1
            }
        )

        builder.then(
            lit("reroll").then(
                arg("index", IntegerArgumentType.integer(1, 3)).executes { context ->
                    if(DailyStreakSettings.dailyStreakEnabled) {
                        val index = IntegerArgumentType.getInteger(context, "index") - 1
                        val data = DailyStreakManager.data
                        val challenge = data.todayChallenges.getOrNull(index)
                        if (challenge != null) {
                            DailyStreakManager.rerollChallenge(challenge.id)
                        } else {
                            Chat.sendMessage(TextUtils.rfuLiteral("${LIGHT_RED}Invalid challenge number!"))
                        }
                    } else {
                        Chat.sendMessage(TextUtils.rfuLiteral("${LIGHT_RED}Daily Streaks are disabled!"))
                    }
                    1
                }
            )
        )
    }
}