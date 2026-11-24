package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerRareDropEvent

@Achievement
object RngManipulationAchievement : BaseAchievement() {
    override val id: String = "rng_manipulation"
    override val name: String = "RNG Manipulation"
    override val description: String = "Drop a Radioactive Vial with 200 Magic Find or more"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    private val DROP = RareDrops.RADIOACTIVE_VIAL

    override fun setupListeners(scope: EventScope) {
        scope.registerRareDropEvent { drop, mf, _ ->
            if(drop == DROP && (mf ?: 0) >= 200) {
                complete()
            }
            true
        }
    }
}