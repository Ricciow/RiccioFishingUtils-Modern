package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent

@Achievement
object DiamondTrophyHunterAchievement : StageAchievement() {
    override val id: String = "diamond_trophy_hunter"
    override val name: String = "Diamond Trophy Hunter"
    override val description: String = "Equip a full set of Diamond Hunter armor."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING
    override val targetStage: Int = 4

    init {
        addStageInfo(1, "Bronze Trophy Hunter", "Equip a full set of Bronze Hunter armor (or higher).", AchievementDifficulty.EASY)
        addStageInfo(2, "Silver Trophy Hunter", "Equip a full set of Silver Hunter armor (or higher).", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gold Trophy Hunter", "Equip a full set of Gold Hunter armor (or higher).", AchievementDifficulty.HARD)
        addStageInfo(4, "Diamond Trophy Hunter", "Equip a full set of Diamond Hunter armor.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerArmorChangeEvent {
            checkArmor()
        })
        checkArmor()
    }

    private fun checkArmor() {
        val minTier = ArmorEvents.currentArmorSet.trophyHunterArmorTier
        if (minTier > 0) {
            while (minTier >= currentStage && !isCompleted) {
                advanceStage()
            }
        }
    }
}
