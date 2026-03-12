package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@Achievement
object FailureIsExpectedAchievement : BaseAchievement() {
    override val id: String = "failure_is_expected"
    override val name: String = "Failure is expected..."
    override val description: String = "Die to jawbus"
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.ISLE

    val DEATH_REGEX = """ ☠ You were killed by Lord Jawbus.""".toExactRegex()

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(DEATH_REGEX) { _, _, _ ->
            complete()
        })
    }
}