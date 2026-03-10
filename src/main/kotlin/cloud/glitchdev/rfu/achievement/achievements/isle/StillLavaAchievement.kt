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
object StillLavaAchievement : NumericStageAchievement() {
    override val id: String = "still_lava"
    override val name: String = "Still Lava"
    override val description: String = "Don't catch a Jawbus for 500/750/1000/1250/1500 catches."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 5

    init {
        addStageInfo(1, "Smoldering Lava", "Don't catch a Jawbus for 500 catches.", AchievementDifficulty.EASY)
        addStageInfo(2, "Flowing Lava", "Don't catch a Jawbus for 750 catches.", AchievementDifficulty.EASY)
        addStageInfo(3, "Bubbly Lava", "Don't catch a Jawbus for 1000 catches.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Calm Lava", "Don't catch a Jawbus for 1250 catches.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Still Lava", "Don't catch a Jawbus for 1500 catches.", AchievementDifficulty.HARD)
    }

    override fun setupListeners() {
        currentCount = catchHistory.getOrAdd(SeaCreatures.JAWBUS).count

        activeListeners.add(registerSeaCreatureCatchEvent { _, _ ->
            currentCount = catchHistory.getOrAdd(SeaCreatures.JAWBUS).count
        })
    }

    override fun getTargetCountForStage(stage: Int): Int {
        return when(stage) {
            1 -> 500
            2 -> 750
            3 -> 1000
            4 -> 1250
            5 -> 1500
            else -> 1500
        }
    }
}
