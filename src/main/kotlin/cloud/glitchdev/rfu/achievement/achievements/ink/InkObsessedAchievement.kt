package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.ink.InkSessionTracker
import cloud.glitchdev.rfu.utils.dsl.compact

@Achievement
object InkObsessedAchievement: NumericStageAchievement() {
    override val id: String = "ink_obsessed"
    override val name: String = "Ink Obsessed"
    override val description: String = "Gain ink collection in one session!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK

    override val targetStage: Int = 4
    override val resetCountOnStageAdvance: Boolean = false

    private val MILESTONES = listOf(
        25_000L, 50_000L, 100_000L, 250_000L
    )

    private val MILESTONE_NAMES = listOf(
        "Locked In", "Need More Ink!", "Time for a Break..?", "Ink Obsessed"
    )

    init {
        MILESTONES.forEachIndexed { index, milestone ->
            var stage = index + 1
            var formatted = milestone.compact()

            var stageDifficulty = when {
                milestone >= 250_000L -> AchievementDifficulty.VERY_HARD
                milestone >= 100_000L  -> AchievementDifficulty.HARD
                milestone >= 50_000L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Gain $formatted ink collection in a single session", stageDifficulty)

        }

    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            val inkSession = InkSessionTracker.totalInk
            currentCount = inkSession.toLong()

        })
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }
}