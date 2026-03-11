package cloud.glitchdev.rfu.achievement.interfaces

import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType

interface IAchievement {
    val id: String
    val name: String
    val description: String
    val type: AchievementType
    val difficulty: AchievementDifficulty
    val category: AchievementCategory
    val isCompleted: Boolean
    val isCheated: Boolean
    val progress: Float // 0.0 to 1.0
    val currentProgress: Int get() = if (isCompleted) 1 else 0
    val targetProgress: Int get() = 1
}