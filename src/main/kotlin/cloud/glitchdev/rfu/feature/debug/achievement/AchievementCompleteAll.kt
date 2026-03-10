package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object AchievementCompleteAll : SimpleCommand("completeall") {
    override val description: String = "Forces completion of all achievements."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!AchievementDebugUtils.checkDevMode(context.source)) return 1

        AchievementManager.getRegistry().values.forEach { it.debugComplete() }
        context.source.sendFeedback(TextUtils.rfuLiteral("Completed all achievements", TextStyle(TextColor.LIGHT_GREEN)))
        return 1
    }
}
