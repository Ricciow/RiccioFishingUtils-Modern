package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession

@Achievement
object MarathonFisherAchievement : NumericStageAchievement() {
    override val id: String = "marathon_fisher"
    override val name: String = "Marathon Fisher"
    override val description: String = "Fish for up to 8 hours in a single session without pausing."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    private val STAGE_TARGET_MINUTES = listOf(60L, 120L, 240L, 360L, 480L)

    init {
        addStageInfo(1, "Dedicated Angler", "Fish for 1 hour in a single session without pausing.", AchievementDifficulty.EASY)
        addStageInfo(2, "Endurance Fisher", "Fish for 2 hours in a single session without pausing.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Session Enthusiast", "Fish for 4 hours in a single session without pausing.", AchievementDifficulty.HARD)
        addStageInfo(4, "Relentless Reeler", "Fish for 6 hours in a single session without pausing.", AchievementDifficulty.VERY_HARD)
        addStageInfo(5, "Marathon Fisher", "Fish for 8 hours in a single session without pausing.", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return STAGE_TARGET_MINUTES.getOrNull(stage - 1) ?: STAGE_TARGET_MINUTES.last()
    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            if (isCompleted) return@registerTickEvent

            if (!FishingSession.isFishing || FishingSession.isPaused) {
                currentCount = 0L
                return@registerTickEvent
            }

            val currentMinutes = FishingSession.duration.inWholeMinutes
            currentCount = currentMinutes
        })
    }
}
