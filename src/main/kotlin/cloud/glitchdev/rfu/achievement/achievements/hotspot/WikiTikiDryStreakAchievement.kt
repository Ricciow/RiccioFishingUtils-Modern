package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object WikiTikiDryStreakAchievement : NumericStageAchievement() {
    override val id: String = "wiki_tiki_dry_streak"
    override val name: String = "Silent Tiki"
    override val description: String = "Don't catch a Wiki Tiki for 500/1000/1500/2000/2500 catches."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    init {
        addStageInfo(1, "Bubbling Waters", "Don't catch a Wiki Tiki for 500 catches.\nMust've caught atleast one Wiki Tiki before.", AchievementDifficulty.EASY)
        addStageInfo(2, "Frothing Surface", "Don't catch a Wiki Tiki for 1000 catches.\nMust've caught atleast one Wiki Tiki before.", AchievementDifficulty.EASY)
        addStageInfo(3, "Disturbed Depths", "Don't catch a Wiki Tiki for 1500 catches.\nMust've caught atleast one Wiki Tiki before.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Shifting Tides", "Don't catch a Wiki Tiki for 2000 catches.\nMust've caught atleast one Wiki Tiki before.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Silent Tiki", "Don't catch a Wiki Tiki for 2500 catches.\nMust've caught atleast one Wiki Tiki before.", AchievementDifficulty.HARD)
    }

    val creature = SeaCreatures.WIKI_TIKI

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
            2 -> 1000L
            3 -> 1500L
            4 -> 2000L
            5 -> 2500L
            else -> 2500L
        }
    }
}
