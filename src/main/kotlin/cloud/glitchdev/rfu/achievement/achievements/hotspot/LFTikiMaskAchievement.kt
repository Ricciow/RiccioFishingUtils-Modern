package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.types.DropDryStreakAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops

@Achievement
object LFTikiMaskAchievement : DropDryStreakAchievement(RareDrops.TIKI_MASK) {
    override val id: String = "lf_mask"
    override val name: String = "LF Mask"
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    override val milestones: List<Long> = listOf(10L, 20L, 30L, 40L, 60L, 80L)
    override val stageNumerals: List<String> = listOf("I", "II", "III", "IV", "V", "VI")
}
