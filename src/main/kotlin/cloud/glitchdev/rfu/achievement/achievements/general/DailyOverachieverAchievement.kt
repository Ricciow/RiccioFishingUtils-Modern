package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents.registerStreakUpdatedEvent

@Achievement
object DailyOverachieverAchievement : NumericStageAchievement() {
    override val id: String = "daily_overachiever"
    override val name: String = "Daily Overachiever"
    override val description: String = "Complete up to 250 total individual daily challenges."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 4
    override val resetCountOnStageAdvance: Boolean = false

    private val STAGE_TARGET_CHALLENGES = listOf(10L, 50L, 100L, 250L)

    init {
        addStageInfo(1, "Task Tackler", "Complete 10 total daily challenges.", AchievementDifficulty.EASY)
        addStageInfo(2, "Task Enthusiast", "Complete 50 total daily challenges.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Goal Getter", "Complete 100 total daily challenges.", AchievementDifficulty.HARD)
        addStageInfo(4, "Daily Overachiever", "Complete 250 total daily challenges.", AchievementDifficulty.VERY_HARD)
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return STAGE_TARGET_CHALLENGES.getOrNull(stage - 1) ?: STAGE_TARGET_CHALLENGES.last()
    }

    override fun setupListeners() {
        activeListeners.add(registerStreakUpdatedEvent { data ->
            if (isCompleted) return@registerStreakUpdatedEvent
            currentCount = data.totalChallengesCompleted.toLong()
        })
    }
}
