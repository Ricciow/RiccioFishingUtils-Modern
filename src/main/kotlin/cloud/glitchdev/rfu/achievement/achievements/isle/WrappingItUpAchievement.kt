package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent

@Achievement
object WrappingItUpAchievement : NumericStageAchievement() {
    override val id: String = "wrapping_it_up"
    override val name: String = "Wrapping it Up"
    override val description: String = "Cocoon each non-hotspot isle sea creature."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 10

    init {
        addStageInfo(1, "Wrapping it up: Magma Slug", "Cocoon a Magma Slug", AchievementDifficulty.EASY)
        addStageInfo(2, "Wrapping it up: Moogma", "Cocoon a Moogma", AchievementDifficulty.EASY)
        addStageInfo(3, "Wrapping it up: Lava Leech", "Cocoon a Lava Leech", AchievementDifficulty.EASY)
        addStageInfo(4, "Wrapping it up: Pyroclastic Worm", "Cocoon a Pyroclastic Worm", AchievementDifficulty.EASY)
        addStageInfo(5, "Wrapping it up: Lava Flame", "Cocoon a Lava Flame", AchievementDifficulty.EASY)
        addStageInfo(6, "Wrapping it up: Fire Eel", "Cocoon a Fire Eel", AchievementDifficulty.MEDIUM)
        addStageInfo(7, "Wrapping it up: Taurus", "Cocoon a Taurus", AchievementDifficulty.MEDIUM)
        addStageInfo(8, "Wrapping it up: Thunder", "Cocoon a Thunder", AchievementDifficulty.HARD)
        addStageInfo(9, "Wrapping it up: Lord Jawbus", "Cocoon a Lord Jawbus", AchievementDifficulty.VERY_HARD)
        addStageInfo(10, "Wrapping it up: Plhlegblast", "Cocoon a Plhlegblast", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun setupListeners() {
        activeListeners.add(registerCocoonEvent { sc ->
            val lookingFor = getCurrentSc() ?: return@registerCocoonEvent
            if (sc.scName == lookingFor) {
                addProgress(1L)
            }
        })
    }

    override fun getTargetCountForStage(stage: Int): Long = 1L

    private fun getCurrentSc() : String? {
        return getStageName(currentStage)?.substringAfter("Wrapping it up: ")
    }
}
