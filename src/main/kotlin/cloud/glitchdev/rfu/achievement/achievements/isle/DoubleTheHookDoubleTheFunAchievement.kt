package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object DoubleTheHookDoubleTheFunAchievement : NumericStageAchievement() {
    override val id: String = "double_the_hook"
    override val name: String = "Double the Hook, Double the fun!"
    override val description: String = "Proc a Double Hook on each non-hotspot isle sea creature."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    override val targetStage: Int = 10

    init {
        addStageInfo(1, "Double Hook: Magma Slug", "Proc a Double Hook on a Magma Slug", AchievementDifficulty.EASY)
        addStageInfo(2, "Double Hook: Moogma", "Proc a Double Hook on a Moogma", AchievementDifficulty.EASY)
        addStageInfo(3, "Double Hook: Lava Leech", "Proc a Double Hook on a Lava Leech", AchievementDifficulty.EASY)
        addStageInfo(4, "Double Hook: Pyroclastic Worm", "Proc a Double Hook on a Pyroclastic Worm", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Double Hook: Lava Flame", "Proc a Double Hook on a Lava Flame", AchievementDifficulty.MEDIUM)
        addStageInfo(6, "Double Hook: Fire Eel", "Proc a Double Hook on a Fire Eel", AchievementDifficulty.MEDIUM)
        addStageInfo(7, "Double Hook: Taurus", "Proc a Double Hook on a Taurus", AchievementDifficulty.HARD)
        addStageInfo(8, "Double Hook: Thunder", "Proc a Double Hook on a Thunder", AchievementDifficulty.HARD)
        addStageInfo(9, "Double Hook: Lord Jawbus", "Proc a Double Hook on a Lord Jawbus", AchievementDifficulty.HARD)
        addStageInfo(10, "Double Hook: Plhlegblast", "Proc a Double Hook on a Plhlegblast", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, doubleHook, _, _, _ ->
            if (doubleHook) {
                val lookingFor = getCurrentSc() ?: return@registerSeaCreatureCatchEvent
                if (sc.scName == lookingFor) {
                    addProgress(1L)
                }
            }
        })
    }

    override fun getTargetCountForStage(stage: Int): Long = 1L

    private fun getCurrentSc() : String? {
        return getStageName(currentStage)?.substringAfter("Double Hook: ")
    }
}
