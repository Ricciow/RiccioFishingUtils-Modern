package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.types.DropDryStreakAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops

@Achievement
object LFTitanoboaShedAchievement : DropDryStreakAchievement(RareDrops.TITANOBOA_SHED) {
    override val id: String = "lf_shed"
    override val name: String = "LF Shed"
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val milestones: List<Long> = listOf(25L, 50L, 75L, 100L, 150L)
}
