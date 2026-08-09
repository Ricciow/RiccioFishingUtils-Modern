package cloud.glitchdev.rfu.feature.streak

import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.gui.window.DailyStreakWindow
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

@Command
object DailyStreakCommand : AbstractCommand("rfudailies") {
    override val description: String = "Opens the daily streaks and challenges window."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            DailyStreakManager.checkDailyReset()
            Gui.openGui(DailyStreakWindow)
            1
        }

        builder.then(
            lit("check").executes {
                DailyStreakManager.checkDailyReset()
                val data = DailyStreakManager.data
                Chat.sendMessage(Component.literal("§b§l[§f§lRFU§b§l] §f\uE11F§6 Daily Streak: §e${data.currentStreak} Days §7(Highest: ${data.highestStreak})"))
                data.todayChallenges.forEachIndexed { index, c ->
                    val status = if (c.isCompleted) "§a✔" else "§c${c.currentProgress}/${c.targetProgress}"
                    Chat.sendMessage(Component.literal("  §7[${index + 1}] §e${c.title}§7: ${c.description} - $status"))
                }
                1
            }
        )
    }
}

@Command
object DailyStreakAliasCommand : AbstractCommand("rfustreak") {
    override val description: String = "Opens the daily streaks and challenges window."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            DailyStreakManager.checkDailyReset()
            Gui.openGui(DailyStreakWindow)
            1
        }
    }
}
