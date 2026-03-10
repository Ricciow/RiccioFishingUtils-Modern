package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object AchievementProgress : AbstractCommand("addprogress") {
    override val description: String = "Adds progress to a numeric achievement."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("id", StringListArgumentType(AchievementDebugUtils.getNumericIds()))
                .then(
                    arg("amount", IntegerArgumentType.integer(1))
                        .executes { context ->
                            if (!AchievementDebugUtils.checkDevMode(context.source)) return@executes 1

                            val id = StringArgumentType.getString(context, "id")
                            val amount = IntegerArgumentType.getInteger(context, "amount")
                            val achievement = AchievementManager.getAchievement(id)

                            if (achievement == null) {
                                context.source.sendFeedback(TextUtils.rfuLiteral("Achievement not found: $id", TextStyle(TextColor.LIGHT_RED)))
                                return@executes 1
                            }

                            if (achievement is NumericAchievement) {
                                achievement.addProgress(amount)
                                context.source.sendFeedback(TextUtils.rfuLiteral("Added $amount progress to: $id", TextStyle(TextColor.LIGHT_GREEN)))
                            } else if (achievement is NumericStageAchievement) {
                                achievement.addProgress(amount) //Different Signature
                                context.source.sendFeedback(TextUtils.rfuLiteral("Added $amount progress to: $id", TextStyle(TextColor.LIGHT_GREEN)))
                            } else {
                                context.source.sendFeedback(TextUtils.rfuLiteral("Achievement is not numeric: $id", TextStyle(TextColor.LIGHT_RED)))
                            }
                            1
                        }
                )
        )
    }
}
