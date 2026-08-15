package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object DoubleHookStreakAchievement : NumericAchievement() {
    override val id: String = "double_hook_streak"
    override val name: String = "Double Madness"
    override val description: String = "Get 8 Double Hooks in a row."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL
    override val targetCount: Long = 8L

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, doubleHook, _, _, _ ->
            if (doubleHook) {
                addProgress()
            } else {
                currentCount = 0L
            }
        })
    }
}
