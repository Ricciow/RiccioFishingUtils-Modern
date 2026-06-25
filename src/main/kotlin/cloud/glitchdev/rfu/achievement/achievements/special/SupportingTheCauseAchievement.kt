package cloud.glitchdev.rfu.achievement.achievements.special

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerChatEvent

@Achievement
object SupportingTheCauseAchievement : BaseAchievement() {
    override val id: String = "supporting_the_cause"
    override val name: String = "Supporting the cause"
    override val description: String = "Vote for Marina in the mayor elections."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.SPECIAL

    private val VOTE_MARINA_REGEX = """You cast \d+ votes for Marina in the Year \d+ Elections!""".toRegex()

    override fun setupListeners() {
        activeListeners.add(registerChatEvent(filter = VOTE_MARINA_REGEX) { _, _ ->
            complete()
        })
    }
}
