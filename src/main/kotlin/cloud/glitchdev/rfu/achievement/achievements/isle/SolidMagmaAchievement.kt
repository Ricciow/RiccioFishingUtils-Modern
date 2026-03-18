package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object SolidMagmaAchievement : NumericStageAchievement() {
    override val id: String = "solid_magma"
    override val name: String = "Solid Magma"
    override val description: String = "Don't catch a Plhlegblast for 250/500/1000/1500/2500 catches."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    init {
        addStageInfo(1, "Churning Magma", "Don't catch a Plhlegblast for 250 catches.\nMust've caught atleast one Plhlegblast before.", AchievementDifficulty.EASY)
        addStageInfo(2, "Rippling Magma", "Don't catch a Plhlegblast for 500 catches.\nMust've caught atleast one Plhlegblast before.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Cooling Magma", "Don't catch a Plhlegblast for 1000 catches.\nMust've caught atleast one Plhlegblast before.", AchievementDifficulty.HARD)
        addStageInfo(4, "Hardened Magma", "Don't catch a Plhlegblast for 1500 catches.\nMust've caught atleast one Plhlegblast before.", AchievementDifficulty.VERY_HARD)
        addStageInfo(5, "Solid Magma", "Don't catch a Plhlegblast for 2500 catches.\nMust've caught atleast one Plhlegblast before.", AchievementDifficulty.IMPOSSIBLE)
    }

    val creature = SeaCreatures.PLHLEGBLAST

    override fun setupListeners() {
        val history = catchHistory.getOrAdd(creature)
        currentCount = if(history.total > 0) {
            history.count.toLong()
        } else {
            0L
        }

        activeListeners.add(registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            val history = catchHistory.getOrAdd(creature)

            currentCount = if(history.total > 0) {
                history.count.toLong()
            } else {
                0L
            }
        })
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return when(stage) {
            1 -> 250L
            2 -> 500L
            3 -> 1000L
            4 -> 1500L
            5 -> 2500L
            else -> 2500L
        }
    }
}
