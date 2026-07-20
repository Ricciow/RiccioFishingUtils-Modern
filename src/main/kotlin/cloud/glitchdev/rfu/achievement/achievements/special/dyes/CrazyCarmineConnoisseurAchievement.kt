package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.DyeAchievement
import cloud.glitchdev.rfu.constants.skyblock.Dyes

@Achievement
object CrazyCarmineConnoisseurAchievement : DyeAchievement(Dyes.CARMINE) {
    override val id: String = "crazy_carmine_connoisseur"
    override val name: String = "Crazy Carmine Connoisseur"
    override val description: String = "Drop a Carmine Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}