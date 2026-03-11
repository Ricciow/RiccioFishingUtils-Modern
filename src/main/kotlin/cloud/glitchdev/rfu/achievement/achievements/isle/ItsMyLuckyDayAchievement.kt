package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent

object ItsMyLuckyDayAchievement : BaseAchievement() {
    override val id: String = "its_my_lucky_day"
    override val name: String = "Its my Lucky Day!"
    override val description: String = "Drop a Radioactive Vial"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners() {
        activeListeners.add(registerRareDropEvent { drop, _ ->
            if(drop == RareDrops.RADIOACTIVE_VIAL) {
                complete()
            }
            true
        })
    }
}