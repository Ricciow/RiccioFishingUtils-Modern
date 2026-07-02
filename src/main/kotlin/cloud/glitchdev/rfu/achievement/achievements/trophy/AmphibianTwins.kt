package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent

@Achievement
object AmphibianTwins : StageAchievement() {
    override val id: String = "amphibian_twins"
    override val name: String = "Amphibian Twins"
    override val description: String = "Catch back-to-back trophy frogs of the same tier."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING
    override val targetStage: Int = 4

    private var lastTrophyTier: String? = null

    private val TROPHY_FROG_REGEX = """♔ TROPHY FROG! You caught (?:an? )?(.+?) (BRONZE|SILVER|GOLD|DIAMOND)!""".toRegex(RegexOption.IGNORE_CASE)

    init {
        addStageInfo(1, "Bronze Amphibian Twins", "Catch two Bronze trophy frogs back-to-back.", AchievementDifficulty.EASY)
        addStageInfo(2, "Silver Amphibian Twins", "Catch two Silver trophy frogs back-to-back.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gold Amphibian Twins", "Catch two Gold trophy frogs back-to-back.", AchievementDifficulty.HARD)
        addStageInfo(4, "Diamond Amphibian Twins", "Catch two Diamond trophy frogs back-to-back.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(TROPHY_FROG_REGEX) { _, _, matches ->
            val tier = matches?.groupValues?.get(2)?.uppercase() ?: return@registerGameEvent
            
            val requiredTier = when (currentStage) {
                1 -> "BRONZE"
                2 -> "SILVER"
                3 -> "GOLD"
                4 -> "DIAMOND"
                else -> return@registerGameEvent
            }
            
            if (tier == requiredTier) {
                if (lastTrophyTier == tier) {
                    lastTrophyTier = null
                    if (currentStage == 4) {
                        complete()
                    } else {
                        advanceStage()
                    }
                } else {
                    lastTrophyTier = tier
                }
            } else {
                lastTrophyTier = tier
            }
        })
    }

    override fun onStageChanged(oldStage: Int, newStage: Int) {
        lastTrophyTier = null
    }

    override fun debugReset() {
        lastTrophyTier = null
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        lastTrophyTier = progressData["lastTrophyTier"] as? String
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        lastTrophyTier?.let { state["lastTrophyTier"] = it }
        return state
    }
}
