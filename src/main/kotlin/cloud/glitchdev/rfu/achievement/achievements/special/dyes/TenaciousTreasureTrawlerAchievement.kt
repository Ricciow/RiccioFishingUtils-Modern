package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.DyeAchievement
import cloud.glitchdev.rfu.constants.skyblock.Dyes

@Achievement
object TenaciousTreasureTrawlerAchievement : DyeAchievement(Dyes.TREASURE) {
    override val id: String = "tenacious_treasure_trawler"
    override val name: String = "Tenacious Treasure Trawler"
    override val description: String = "Drop a Treasure Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}