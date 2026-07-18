package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.fishing.TrophyTier
import cloud.glitchdev.rfu.data.fishing.TrophyDataManager
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent

@Achievement
object FrogPityAchievement : BaseAchievement() {
    override val id: String = "trophy_frog_pity"
    override val name: String = "Frog Pity"
    override val description: String = "Catch a Diamond trophy frog on pity."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING

    override fun setupListeners() {
        activeListeners.add(registerTrophyFrogCatchEvent(priority = 10) { frog, tier ->
            if (tier == TrophyTier.DIAMOND) {
                val existing = TrophyDataManager.data.pity.frogPity[frog.name]
                if (existing != null && existing.diamondProgress >= frog.diamondPity) {
                    complete()
                }
            }
        })
    }
}
