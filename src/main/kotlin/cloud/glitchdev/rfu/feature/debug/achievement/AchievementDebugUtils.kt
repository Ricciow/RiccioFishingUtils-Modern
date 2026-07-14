package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.achievement.types.StageAchievement

object AchievementDebugUtils {
    fun getAllIds(): List<String> = AchievementManager.getRegistry().keys.toList()

    fun getStageIds(): List<String> = AchievementManager.getRegistry().values
        .filterIsInstance<StageAchievement>()
        .map { it.id }

    fun getNumericIds(): List<String> = AchievementManager.getRegistry().values
        .filter { it is NumericAchievement || it is NumericStageAchievement }
        .map { it.id }
}
