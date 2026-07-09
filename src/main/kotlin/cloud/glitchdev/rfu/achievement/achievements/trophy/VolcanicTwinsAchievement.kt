package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.constants.fishing.TrophyTier

@Achievement
object VolcanicTwinsAchievement : StageAchievement() {
    override val id: String = "volcanic_twins"
    override val name: String = "Volcanic Twins"
    override val description: String = "Catch back-to-back trophy fish of the same tier."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING
    override val targetStage: Int = 4

    private var lastTrophyTier: String? = null

    init {
        addStageInfo(1, "Bronze Volcanic Twins", "Catch two Bronze trophy fish back-to-back.", AchievementDifficulty.EASY)
        addStageInfo(2, "Silver Volcanic Twins", "Catch two Silver trophy fish back-to-back.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gold Volcanic Twins", "Catch two Gold trophy fish back-to-back.", AchievementDifficulty.HARD)
        addStageInfo(4, "Diamond Volcanic Twins", "Catch two Diamond trophy fish back-to-back.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerTrophyFishCatchEvent { _, tier ->
            val requiredTier = when (currentStage) {
                1 -> TrophyTier.BRONZE
                2 -> TrophyTier.SILVER
                3 -> TrophyTier.GOLD
                4 -> TrophyTier.DIAMOND
                else -> return@registerTrophyFishCatchEvent
            }
            
            val tierStr = tier.name
            if (tier == requiredTier) {
                if (lastTrophyTier == tierStr) {
                    lastTrophyTier = null
                    if (currentStage == 4) {
                        complete()
                    } else {
                        advanceStage()
                    }
                } else {
                    lastTrophyTier = tierStr
                }
            } else {
                lastTrophyTier = tierStr
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
