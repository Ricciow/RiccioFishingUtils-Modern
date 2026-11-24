package cloud.glitchdev.rfu.achievement.achievements.ink

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.fishing.registerSeaCreatureCatchEvent

@Achievement
object SquidStreakAchievement: NumericAchievement() {
    override val id: String = "squid_streak"
    override val name: String = "Only Squids"
    override val description: String = "Catch 12 regular Squids in a row!"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.INK
    override val targetCount: Long = 12L

    override fun setupListeners(scope: EventScope) {
        scope.registerSeaCreatureCatchEvent { sc, _, _, _, _ ->
            if (sc.scName == "Squid") {
                addProgress()
            } else {
                currentCount = 0L
            }
        }
    }
}