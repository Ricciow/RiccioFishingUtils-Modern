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
object SunnyDayAchievement : NumericStageAchievement() {
    override val id: String = "sunny_day"
    override val name: String = "Sunny Day"
    override val description: String = "Don't catch a Thunder for 100/150/200/250/300 catches."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    init {
        addStageInfo(1, "Rainy Day", "Don't catch a Thunder for 100 catches.\nMust've caught atleast one Thunder before.", AchievementDifficulty.EASY)
        addStageInfo(2, "Overcast Day", "Don't catch a Thunder for 150 catches.\nMust've caught atleast one Thunder before.", AchievementDifficulty.EASY)
        addStageInfo(3, "Cloudy Day", "Don't catch a Thunder for 200 catches.\nMust've caught atleast one Thunder before.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Clear Sky Day", "Don't catch a Thunder for 250 catches.\nMust've caught atleast one Thunder before.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Sunny Day", "Don't catch a Thunder for 300 catches.\nMust've caught atleast one Thunder before.", AchievementDifficulty.MEDIUM)
    }

    val creature = SeaCreatures.THUNDER

    override fun setupListeners() {
        val history = catchHistory.getOrAdd(creature)
        currentCount = if(history.total > 0) {
            history.count
        } else {
            0
        }

        activeListeners.add(registerSeaCreatureCatchEvent { _, _ ->
            val history = catchHistory.getOrAdd(creature)

            currentCount = if(history.total > 0) {
                history.count
            } else {
                0
            }
        })
    }

    override fun getTargetCountForStage(stage: Int): Int {
        return when(stage) {
            1 -> 100
            2 -> 150
            3 -> 200
            4 -> 250
            5 -> 300
            else -> 300
        }
    }
}
