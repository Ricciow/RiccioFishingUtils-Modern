package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerRareDropEvent

object ItsMyLuckyDayAchievement : BaseAchievement() {
    override val id: String = "its_my_lucky_day"
    override val name: String = "Its my Lucky Day!"
    override val description: String = "Drop a Radioactive Vial"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners(scope: EventScope) {
        scope.registerRareDropEvent { drop, _, _ ->
            if(drop == RareDrops.RADIOACTIVE_VIAL) {
                complete()
            }
            true
        }
    }
}