package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement

import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent

@Achievement
object OrderingTakeOutAchievement : NumericStageAchievement() {
    override val id: String = "ordering_take_out"
    override val name: String = "Ordering Take Out"
    override val description: String = "Cocoon a massive number of sea creatures."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 6
    override val resetCountOnStageAdvance: Boolean = false

    private val MILESTONES = listOf(10L, 50L, 250L, 750L, 1500L, 2500L)

    init {
        addStageInfo(1, "Ordering a Light Snack", "Cocoon 10 sea creatures", AchievementDifficulty.EASY)
        addStageInfo(2, "Ordering for a Friend", "Cocoon 50 sea creatures", AchievementDifficulty.EASY)
        addStageInfo(3, "Ordering for a Party", "Cocoon 250 sea creatures", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Ordering for a Feast", "Cocoon 750 sea creatures", AchievementDifficulty.HARD)
        addStageInfo(5, "Ordering for a Banquet", "Cocoon 1500 sea creatures", AchievementDifficulty.VERY_HARD)
        addStageInfo(6, "Ordering Take Out", "Cocoon 2500 sea creatures", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun setupListeners() {
        activeListeners.add(registerCocoonEvent { _ ->
            addProgress(1L)
        })
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }
}
