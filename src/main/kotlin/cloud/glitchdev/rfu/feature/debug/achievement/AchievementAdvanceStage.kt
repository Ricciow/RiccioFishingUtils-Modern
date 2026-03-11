package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object AchievementAdvanceStage : AbstractCommand("advancestage") {
    override val description: String = "Advances the stage of a stage achievement."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("id", StringListArgumentType(AchievementDebugUtils.getStageIds()))
                .executes { context ->
                    if (!AchievementDebugUtils.checkDevMode(context.source)) return@executes 1

                    val id = StringArgumentType.getString(context, "id")
                    val achievement = AchievementManager.getAchievement(id)

                    if (achievement == null) {
                        context.source.sendFeedback(TextUtils.rfuLiteral("Achievement not found: $id", TextStyle(TextColor.LIGHT_RED)))
                        return@executes 1
                    }

                    if (achievement is StageAchievement) {
                        achievement.debugAdvanceStage()
                        context.source.sendFeedback(TextUtils.rfuLiteral("Advanced stage for: $id", TextStyle(TextColor.LIGHT_GREEN)))
                    } else {
                        context.source.sendFeedback(TextUtils.rfuLiteral("Achievement is not a stage achievement: $id", TextStyle(TextColor.LIGHT_RED)))
                    }
                    1
                }
        )
    }
}
