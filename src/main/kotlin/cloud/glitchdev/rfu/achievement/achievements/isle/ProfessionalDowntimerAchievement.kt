package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@Achievement
object ProfessionalDowntimerAchievement : BaseAchievement() {
    override val id: String = "professional_downtimer"
    override val name: String = "Professional Downtimer"
    override val description: String = "Die to a vanquisher or lava."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.ISLE

    val DEATH_REGEX = """ ☠ You (burned to death|were killed by Vanquisher).""".toExactRegex()

    override fun setupListeners() {
        registerGameEvent(DEATH_REGEX) { _, _, _ ->
            complete()
        }
    }
}
