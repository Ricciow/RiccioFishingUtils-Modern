package cloud.glitchdev.rfu.achievement.achievements.hotspot

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.types.DropDryStreakAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops

@Achievement
object LFBurntTextsAchievement : DropDryStreakAchievement(RareDrops.BURNT_TEXTS) {
    override val id: String = "lf_texts"
    override val name: String = "LF Texts"
    override val category: AchievementCategory = AchievementCategory.HOT_SPOT

    override val milestones: List<Long> = listOf(10L, 20L, 30L, 40L, 50L)
}
