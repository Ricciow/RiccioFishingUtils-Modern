package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.achievement.types.CompletionAchievement

@Achievement
object FabulousFishingFancierAchievement : CompletionAchievement() {
    override val achievements: List<BaseAchievement> = listOf(AuspiciousAquamarineAffinityAchievement,
        CrazyCarmineConnoisseurAchievement, IridescentIcebergIntellectualAchievement, MajesticMidnightManiacAchievement,
        TenaciousTreasureTrawlerAchievement)
    override val id: String = "fabulous_fishing_fancier"
    override val name: String = "Fabulous Fishing Fancier"
    override val description: String = "Get all 5 true fishing dyes"
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}