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
object      StillLavaAchievement : NumericStageAchievement() {
    override val id: String = "still_lava"
    override val name: String = "Still Lava"
    override val description: String = "Don't catch a Jawbus for 500/750/1000/1250/1500 catches."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    init {
        addStageInfo(1, "Smoldering Lava", "Don't catch a Jawbus for 500 catches.\nMust've caught atleast one Jawbus before.", AchievementDifficulty.EASY)
        addStageInfo(2, "Flowing Lava", "Don't catch a Jawbus for 750 catches.\nMust've caught atleast one Jawbus before.", AchievementDifficulty.EASY)
        addStageInfo(3, "Bubbly Lava", "Don't catch a Jawbus for 1000 catches.\nMust've caught atleast one Jawbus before.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Calm Lava", "Don't catch a Jawbus for 1250 catches.\nMust've caught atleast one Jawbus before.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Still Lava", "Don't catch a Jawbus for 1500 catches.\nMust've caught atleast one Jawbus before.", AchievementDifficulty.HARD)
    }

    val creature = SeaCreatures.JAWBUS

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
            1 -> 500L
            2 -> 750L
            3 -> 1000L
            4 -> 1250L
            5 -> 1500L
            else -> 1500L
        }
    }
}
