package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@Achievement
object SoThisIsWhatHardModeDoesAchievement : BaseAchievement() {
    override val id: String = "so_this_is_what_hardmode_does"
    override val name: String = "So this is what hard mode does..."
    override val description: String = "Find out what hard mode does!"
    override val type: AchievementType = AchievementType.HIDDEN
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.ISLE

    val DEATH_REGEX = """ ☠ You were killed by Lord Jawbus.""".toExactRegex()

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(DEATH_REGEX) { _, _, _ ->
            if(LavaFishing.jawbus_hard_mode) {
                complete()
            }
        })
    }
}
