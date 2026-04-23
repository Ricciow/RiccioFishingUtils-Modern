package cloud.glitchdev.rfu.achievement.achievements.special.dyes

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.DyeAchievement
import cloud.glitchdev.rfu.constants.Dyes

@Achievement
object ThatsAFishingDyeAchievement : DyeAchievement(Dyes.BONE) {
    override val id: String = "thats_a_fishing_dye"
    override val name: String = "Thats a fishing dye!?"
    override val description: String = "Drop a Bone Dye."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL
}