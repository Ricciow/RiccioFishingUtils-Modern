package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent

@Achievement
object FriendlyFisherAchievement : NumericStageAchievement() {
    override val id: String = "friendly_fisher"
    override val name: String = "Friendly Fisher"
    override val description: String = "Equip a Bobbin' Time 3/4/5 full armor set."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.GENERAL
    override val targetStage: Int = 3

    init {
        addStageInfo(1, "Polite Fisher", "Equip a Bobbin' Time 3+ full armor set.", AchievementDifficulty.EASY)
        addStageInfo(2, "Neighbourly Fisher", "Equip a Bobbin' Time 4+ full armor set.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Friendly Fisher", "Equip a Bobbin' Time 5 full armor set.", AchievementDifficulty.MEDIUM)
    }

    override fun setupListeners() {
        activeListeners.add(registerArmorChangeEvent {
            checkAll()
        })
        checkAll()
    }

    private fun checkAll() {
        while (!isCompleted) {
            val targetLevel = getTargetBobbinLevelForStage(currentStage)
            val count = ArmorEvents.currentArmorSet.getValidBobbinCount(targetLevel).toLong()
            currentCount = count
            if (count < targetCount) break
        }
    }

    override fun getTargetCountForStage(stage: Int): Long = 4L

    private fun getTargetBobbinLevelForStage(stage: Int): Int = (stage + 2).coerceAtMost(5)
}