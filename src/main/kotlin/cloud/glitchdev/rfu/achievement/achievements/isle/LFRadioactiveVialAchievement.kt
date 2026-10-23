package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.types.DropDryStreakAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops

@Achievement
object LFRadioactiveVialAchievement : DropDryStreakAchievement(RareDrops.RADIOACTIVE_VIAL) {
    override val id: String = "lf_vial"
    override val name: String = "LF Vial"
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val milestones: List<Long> = listOf(20L, 40L, 60L, 80L, 100L)
    override val stageNumerals: List<String> = listOf("I", "II", "III", "IV", "V")
}
