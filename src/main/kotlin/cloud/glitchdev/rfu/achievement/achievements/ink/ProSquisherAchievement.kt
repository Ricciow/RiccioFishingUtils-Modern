package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.data.collections.CollectionItem
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.events.managers.CollectionEvents.registerCollectionUpdateEvent
import cloud.glitchdev.rfu.utils.dsl.compact


@Achievement
object ProSquisherAchievement: NumericStageAchievement() {
    override val id: String = "pro_squisher"
    override val name: String = "Pro Squisher"
    override val description: String = "Gain ink collection!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.INK

    override val targetStage: Int = 8
    override val resetCountOnStageAdvance: Boolean = false

    private val MILESTONES = listOf(
        10_000L, 50_000L, 100_000L, 250_000L, 500_000L, 1_000_000L, 2_500_000L, 5_000_000L
    )

    private val MILESTONE_NAMES = listOf(
        "Baby Squisher", "Fledgling Squisher", "New Squisher", "Starting Squisher",
        "Intermediate Squisher", "Advanced Squisher", "Proficient Squisher", "Pro Squisher"
    )

    init {
        MILESTONES.forEachIndexed { index, milestone ->
            val stage = index + 1
            val formatted = milestone.compact()


            val stageDifficulty = when {
                milestone >= 5_000_000L -> AchievementDifficulty.IMPOSSIBLE
                milestone >= 1_000_000L -> AchievementDifficulty.VERY_HARD
                milestone >= 500_000L -> AchievementDifficulty.HARD
                milestone >= 100_000L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Reach $formatted Ink Collection", stageDifficulty)

        }

    }

    override fun setupListeners() {
        currentCount = CollectionsHandler.get(CollectionItem.INK_SAC)
        while (!isCompleted && currentCount >= targetCount) {
            advanceStage()
        }

        activeListeners.add(registerCollectionUpdateEvent { item, _, total, _ ->
            if (item == CollectionItem.INK_SAC) {
                currentCount = total
                while (!isCompleted && currentCount >= targetCount) {
                    advanceStage()
                }
            }
        })
    }



    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }
}
