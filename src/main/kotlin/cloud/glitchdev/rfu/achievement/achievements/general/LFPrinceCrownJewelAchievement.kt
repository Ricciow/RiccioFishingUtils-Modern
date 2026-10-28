package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.types.DropDryStreakAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops

@Achievement
object LFPrinceCrownJewelAchievement : DropDryStreakAchievement(RareDrops.PRINCE_CROWN_JEWEL) {
    override val id: String = "lf_jewel"
    override val name: String = "LF Jewel"
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val milestones: List<Long> = listOf(25L, 50L, 75L, 100L, 150L)
}
