package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents.registerStreakUpdatedEvent

@Achievement
object DailyStreakAchievement : NumericStageAchievement() {
    override val id: String = "daily_streak_master"
    override val name: String = "Streak Master"
    override val description: String = "Maintain a daily fishing streak for up to 365 days (1 year)."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 8
    override val resetCountOnStageAdvance: Boolean = false

    private val STAGE_TARGET_DAYS = listOf(3L, 7L, 14L, 30L, 60L, 100L, 180L, 365L)

    init {
        addStageInfo(1, "Streak Starter", "Maintain a 3-day daily streak.", AchievementDifficulty.EASY)
        addStageInfo(2, "Weekly Habit", "Maintain a 7-day daily streak.", AchievementDifficulty.EASY)
        addStageInfo(3, "Fortnight Fisher", "Maintain a 14-day daily streak.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Monthly Reeler", "Maintain a 30-day daily streak.", AchievementDifficulty.HARD)
        addStageInfo(5, "Seasoned Angler", "Maintain a 60-day daily streak.", AchievementDifficulty.HARD)
        addStageInfo(6, "Centurion Angler", "Maintain a 100-day daily streak.", AchievementDifficulty.VERY_HARD)
        addStageInfo(7, "Half-Year Mariner", "Maintain a 180-day daily streak.", AchievementDifficulty.VERY_HARD)
        addStageInfo(8, "Year of the Reeler", "Maintain a 365-day daily streak.", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return STAGE_TARGET_DAYS.getOrNull(stage - 1) ?: STAGE_TARGET_DAYS.last()
    }

    override fun setupListeners() {
        activeListeners.add(registerStreakUpdatedEvent { data ->
            if (isCompleted) return@registerStreakUpdatedEvent
            currentCount = data.currentStreak.toLong()
        })
    }
}
