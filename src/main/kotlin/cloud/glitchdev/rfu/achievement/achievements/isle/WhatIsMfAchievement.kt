package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent

@Achievement
object WhatIsMfAchievement : BaseAchievement() {
    override val id: String = "what_is_mf"
    override val name: String = "What is Magic Find?"
    override val description: String = "Drop a Radioactive Vial with 200 Magic Find or less"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    private val DROP = RareDrops.RADIOACTIVE_VIAL

    override fun setupListeners() {
        activeListeners.add(registerRareDropEvent { drop, mf ->
            if(drop == DROP && (mf ?: 0) <= 200) {
                complete()
            }
            true
        })
    }
}
