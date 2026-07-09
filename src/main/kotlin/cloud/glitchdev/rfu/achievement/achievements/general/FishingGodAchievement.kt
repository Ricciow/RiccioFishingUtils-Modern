package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.events.managers.SkillEvents.registerSkillXpUpdateEvent
import cloud.glitchdev.rfu.utils.SkillTracker

@Achievement
object FishingGodAchievement : NumericStageAchievement() {
    override val id = "fishing_god"
    override val name = "Fishing God"
    override val description = "Accumulate massive amounts of fishing experience."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.IMPOSSIBLE
    override val category = AchievementCategory.GENERAL

    override val targetStage = 15
    override val resetCountOnStageAdvance = false

    private val MILESTONES = listOf(
        25_000_000L, 50_000_000L, 100_000_000L, 250_000_000L, 500_000_000L,
        750_000_000L, 1_000_000_000L, 1_500_000_000L, 2_000_000_000L, 2_500_000_000L,
        3_000_000_000L, 3_500_000_000L, 4_000_000_000L, 4_500_000_000L, 5_000_000_000L
    )

    private val MILESTONE_NAMES = listOf(
        "Novice Angler", "Skilled Fisherman", "Master Caster", "Elite Reeler", "Oceanic Explorer",
        "Abyssal Champion", "Legendary Harpooner", "Tidal King", "Poseidon's Enemy", "World Class Fisher",
        "Mythical Mariner", "Cosmic Angler", "Eternal Fisher", "Omnipotent Angler", "Fishing God"
    )

    init {
        MILESTONES.forEachIndexed { index, milestone ->
            val stage = index + 1
            val formatted = formatXp(milestone)
            
            val stageDifficulty = when {
                milestone >= 1_000_000_000L -> AchievementDifficulty.IMPOSSIBLE
                milestone >= 750_000_000L -> AchievementDifficulty.VERY_HARD
                milestone >= 500_000_000L -> AchievementDifficulty.HARD
                milestone >= 250_000_000L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Reach $formatted Fishing XP", stageDifficulty)
        }
    }

    private fun formatXp(xp: Long): String {
        return when {
            xp >= 1_000_000_000L -> "${xp / 1_000_000_000.0}B"
            xp >= 1_000_000L -> "${xp / 1_000_000.0}M"
            xp >= 1_000L -> "${xp / 1_000.0}k"
            else -> xp.toString()
        }.replace(".0", "")
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }

    var totalFishingXp: Long = 0
        private set(value) {
            field = value
            currentCount = value
            while (!isCompleted && currentCount >= targetCount) {
                advanceStage()
            }
        }

    override fun setupListeners() {
        activeListeners.add(registerSkillXpUpdateEvent(SkillType.FISHING) { _, xp ->
            totalFishingXp = xp
        })

        totalFishingXp = SkillTracker.getSkillXp(SkillType.FISHING)
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["totalFishingXp"] = totalFishingXp
        return state
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        totalFishingXp = (progressData["totalFishingXp"] as? Number)?.toLong() ?: 0L
    }
}
