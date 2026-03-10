package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object AchievementDebugUtils {
    fun getAllIds(): List<String> = AchievementManager.getRegistry().keys.toList()

    fun getStageIds(): List<String> = AchievementManager.getRegistry().values
        .filterIsInstance<StageAchievement>()
        .map { it.id }

    fun getNumericIds(): List<String> = AchievementManager.getRegistry().values
        .filter { it is NumericAchievement || it is NumericStageAchievement }
        .map { it.id }

    fun checkDevMode(source: FabricClientCommandSource): Boolean {
        if (!DevSettings.devMode) {
            source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must have developer mode on to use this feature!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.BOLD)
                )
            )
            return false
        }
        return true
    }
}
