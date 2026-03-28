package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent


@Achievement
object LotteryTicketAchievement: NumericStageAchievement() {
    override val id: String = "lottery_ticket"
    override val name: String = "Buy a Lottery Ticket"
    override val description: String = "Drop lucky clover cores!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK

    override val targetStage: Int = 4
    override val resetCountOnStageAdvance: Boolean = false

    private val MILESTONES = listOf(
        1L, 10L, 50L, 100L
    )

    private val MILESTONE_NAMES = listOf(
        "First Taste of Luck", "Lucky Person", "Four-Leaf Finder", "Buy a Lottery Ticket"
    )



    init {
        MILESTONES.forEachIndexed { index, milestone ->
            var stage = index + 1

            var stageDifficulty = when {
                milestone >= 100L -> AchievementDifficulty.VERY_HARD
                milestone >= 50L  -> AchievementDifficulty.HARD
                milestone >= 10L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Drop $milestone Lucky Clover Cores", stageDifficulty)

        }

    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            val totalCores = DropManager.dropHistory.getOrAdd(RareDrops.LUCKY_CLOVER_CORE).history.size
            currentCount = totalCores.toLong()

        })
    }



    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }


}