package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.DyeAchievement
import cloud.glitchdev.rfu.constants.skyblock.Dyes

@Achievement
object AuspiciousAquamarineAffinityAchievement : DyeAchievement(Dyes.AQUAMARINE) {
    override val id: String = "auspicious_aquamarine_affinity"
    override val name: String = "Auspicious Aquamarine Affinity"
    override val description: String = "Drop an Aquamarine Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}