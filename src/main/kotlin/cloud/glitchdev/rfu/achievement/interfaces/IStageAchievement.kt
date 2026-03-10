package cloud.glitchdev.rfu.achievement.interfaces

import cloud.glitchdev.rfu.achievement.AchievementDifficulty

interface IStageAchievement : IAchievement {
    val currentStage: Int
    val targetStage: Int

    fun getStageName(stage: Int): String? = null
    fun getStageDescription(stage: Int): String? = null
    fun getStageDifficulty(stage: Int): AchievementDifficulty? = null
}
