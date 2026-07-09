package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.DyeAchievement
import cloud.glitchdev.rfu.constants.skyblock.Dyes

@Achievement
object IridescentIcebergIntellectualAchievement : DyeAchievement(Dyes.ICEBERG) {
    override val id: String = "iridescent_iceberg_intellectual"
    override val name: String = "Iridescent Iceberg Intellectual"
    override val description: String = "Drop an Iceberg Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}