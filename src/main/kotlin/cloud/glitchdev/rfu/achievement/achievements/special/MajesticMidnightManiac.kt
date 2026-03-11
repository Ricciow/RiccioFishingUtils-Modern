package cloud.glitchdev.rfu.achievement.achievements.special

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.events.managers.DropEvents

@Achievement
object MajesticMidnightManiac : BaseAchievement() {
    override val id: String = "majestic_midnight_maniac"
    override val name: String = "Majestic Midnight Maniac"
    override val description: String = "Drop a Midnight Dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.SPECIAL

    override fun setupListeners() {
        DropEvents.registerDyeDropEvent { dyeDrop, _ ->
            if (dyeDrop == Dyes.MIDNIGHT) {
                complete()
            }
        }
    }
}