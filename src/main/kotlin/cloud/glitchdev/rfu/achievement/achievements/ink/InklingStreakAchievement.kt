package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object InklingStreakAchievement: NumericAchievement() {
    override val id: String = "night_squid_streak"
    override val name: String = "Pitch Black"
    override val description: String = "Catch 6 Inklings in a row!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK
    override val targetCount: Long = 6L

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent
        { sc, _, _, _, _ ->
            val inkling = SeaCreatures.get("Inkling")
            if (inkling != null && sc == inkling) {
                addProgress()
            } else {
                currentCount = 0L
            }
        })
    }

}
