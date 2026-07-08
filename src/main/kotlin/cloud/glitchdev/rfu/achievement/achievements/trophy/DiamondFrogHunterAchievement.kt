package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString

@Achievement
object DiamondFrogHunterAchievement : StageAchievement() {
    override val id: String = "diamond_frog_hunter"
    override val name: String = "Diamond Frog Hunter"
    override val description: String = "Hold a PhD's Grimoire."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING
    override val targetStage: Int = 4

    init {
        addStageInfo(1, "Bronze Frog Hunter", "Hold an Applicant's Statement (or higher).", AchievementDifficulty.EASY)
        addStageInfo(2, "Silver Frog Hunter", "Hold a Student's Studies (or higher).", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gold Frog Hunter", "Hold a Master's Thesis (or higher).", AchievementDifficulty.HARD)
        addStageInfo(4, "Diamond Frog Hunter", "Hold a PhD's Grimoire.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 40) {
            val player = mc.player ?: return@registerTickEvent

            val mainHandName = player.mainHandItem.customName?.toUnformattedString()
            val offHandName = player.offhandItem.customName?.toUnformattedString()

            val mainTier = mainHandName?.let { getFrogHunterItemTier(it) } ?: 0
            val offTier = offHandName?.let { getFrogHunterItemTier(it) } ?: 0

            val currentHeldTier = maxOf(mainTier, offTier)

            if (currentHeldTier > 0) {
                while (currentHeldTier >= currentStage && !isCompleted) {
                    advanceStage()
                }
            }
        })
    }

    private fun getFrogHunterItemTier(itemName: String): Int {
        return when {
            itemName.contains("PhD's Grimoire") -> 4
            itemName.contains("Master's Thesis") -> 3
            itemName.contains("Student's Studies") -> 2
            itemName.contains("Applicant's Statement") -> 1
            else -> 0
        }
    }
}
