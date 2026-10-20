package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAnyChatEvent
import cloud.glitchdev.rfu.feature.fishing.MobyDuckFeature
import cloud.glitchdev.rfu.utils.World

@Achievement
object WellReadAchievement : NumericStageAchievement() {
    override val id: String = "well_read"
    override val name: String = "Well Read"
    override val description: String = "Consume 1/10/25/50/100 Collector's Edition Moby-Ducks."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 5
    override val resetCountOnStageAdvance: Boolean = false

    private val MILESTONES = listOf(1L, 10L, 25L, 50L, 100L)

    init {
        addStageInfo(1, "First Chapter", "Consume 1 Collector's Edition Moby-Duck.", AchievementDifficulty.EASY)
        addStageInfo(2, "Page Turner", "Consume 10 Collector's Edition Moby-Ducks.", AchievementDifficulty.EASY)
        addStageInfo(3, "Bookworm", "Consume 25 Collector's Edition Moby-Ducks.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Avid Reader", "Consume 50 Collector's Edition Moby-Ducks.", AchievementDifficulty.HARD)
        addStageInfo(5, "Well Read", "Consume 100 Collector's Edition Moby-Ducks.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        val (chatEvent, gameEvent) = registerAnyChatEvent(MobyDuckFeature.mobyDuckRegex) { _, match ->
            if (World.isInSkyblock && match?.groupValues?.getOrNull(1) == "Collector's Edition") {
                addProgress()
            }
        }
        activeListeners.add(chatEvent)
        activeListeners.add(gameEvent)
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }
}
