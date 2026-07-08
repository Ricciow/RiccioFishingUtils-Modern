package cloud.glitchdev.rfu.achievement.achievements.special

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement

@Achievement
object TouchGrassAchievement : BaseAchievement() {
    override val id: String = "touch_grass"
    override val name: String = "Touch Grass"
    override val description: String = "Ask yourself how long it has been since you touched grass."
    override val type: AchievementType = AchievementType.HIDDEN
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.SPECIAL

    override fun setupListeners() {
        // Triggered directly by SinceCommand when '!since grass' is executed
    }

    fun trigger() {
        complete()
    }
}
