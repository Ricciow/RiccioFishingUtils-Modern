package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString

@Achievement
object NowThisIsGiantAchievement : BaseAchievement() {
    override val id: String = "now_this_is_giant"
    override val name: String = "Now this is Giant!"
    override val description: String = "Hold a Giant Rod."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 100) {
            if(mc.player?.mainHandItem?.customName?.toUnformattedString()?.contains("Giant Fishing Rod") ?: false) {
                complete()
            }
        })
    }
}