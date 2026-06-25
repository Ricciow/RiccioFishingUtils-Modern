package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyJoinedEvent

@Achievement
object PartyExplorerAchievement : NumericStageAchievement() {
    override val id: String = "party_explorer"
    override val name: String = "Party Explorer"
    override val description: String = "Join parties or host players through the RFU Party Finder."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 10
    override val resetCountOnStageAdvance: Boolean = false

    init {
        addStageInfo(1, "Social Butterfly", "Join or host 1 player through the RFU Party Finder.", AchievementDifficulty.EASY)
        addStageInfo(2, "Frequent Flyer", "Join or host 5 players through the RFU Party Finder.", AchievementDifficulty.EASY)
        addStageInfo(3, "Group Gatherer", "Join or host 25 players through the RFU Party Finder.", AchievementDifficulty.EASY)
        addStageInfo(4, "Party Hopper", "Join or host 100 players through the RFU Party Finder.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Socialite", "Join or host 250 players through the RFU Party Finder.", AchievementDifficulty.MEDIUM)
        addStageInfo(6, "Networking Ninja", "Join or host 500 players through the RFU Party Finder.", AchievementDifficulty.HARD)
        addStageInfo(7, "Community Connector", "Join or host 1000 players through the RFU Party Finder.", AchievementDifficulty.HARD)
        addStageInfo(8, "Squad Specialist", "Join or host 1500 players through the RFU Party Finder.", AchievementDifficulty.VERY_HARD)
        addStageInfo(9, "Assembly Ambassador", "Join or host 2000 players through the RFU Party Finder.", AchievementDifficulty.VERY_HARD)
        addStageInfo(10, "Party Legend", "Join or host 2500 players through the RFU Party Finder.", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun setupListeners() {
        activeListeners.add(registerPartyJoinedEvent {
            addProgress(1)
        })
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return when (stage) {
            1 -> 1L
            2 -> 5L
            3 -> 25L
            4 -> 100L
            5 -> 250L
            6 -> 500L
            7 -> 1000L
            8 -> 1500L
            9 -> 2000L
            10 -> 2500L
            else -> 2500L
        }
    }
}
