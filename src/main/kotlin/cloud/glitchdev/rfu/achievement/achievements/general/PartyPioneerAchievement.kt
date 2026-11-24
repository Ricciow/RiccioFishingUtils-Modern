package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.party.registerPartyCreatedEvent

@Achievement
object PartyPioneerAchievement : BaseAchievement() {
    override val id: String = "party_pioneer"
    override val name: String = "Party Pioneer"
    override val description: String = "Create a party in the RFU Party Finder."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.EASY
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override fun setupListeners(scope: EventScope) {
        scope.registerPartyCreatedEvent {
            complete()
        }
    }
}
