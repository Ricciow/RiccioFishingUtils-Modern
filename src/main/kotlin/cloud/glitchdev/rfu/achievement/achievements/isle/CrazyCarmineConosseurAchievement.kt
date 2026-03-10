package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.events.managers.DropEvents.registerDyeDropEvent

@Achievement
object CrazyCarmineConosseurAchievement : BaseAchievement() {
    override val id: String = "crazy_carmine_conosseur"
    override val name: String = "Crazy Carmine Conosseur"
    override val description: String = "Drop a carmine dye."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners() {
        registerDyeDropEvent { dyeDrop, _ ->
            if(dyeDrop == Dyes.CARMINE) {
                complete()
            }
        }
    }
}