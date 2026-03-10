package cloud.glitchdev.rfu.achievement.achievements.special

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.utils.Tablist

@Achievement
object TheCreatorAchievement : BaseAchievement() {
    override val id: String = "the_creator"
    override val name: String = "The Creator"
    override val description: String = "Be on the same lobby as ricciow, the creator."
    override val type: AchievementType = AchievementType.HIDDEN
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.SPECIAL

    private val CREATOR_USERNAME = "ricciow"

    override fun setupListeners() {
        activeListeners.add(TickEvents.registerTickEvent(interval = 100) {
            val players = Tablist.getPlayerNames().toSet()

            if (players.contains(CREATOR_USERNAME)) {
                complete()
            }
        })
    }
}