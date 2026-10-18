package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.events.managers.SeaCreaturePersonalBestEvents.registerSeaCreaturePersonalBestEvent

@Achievement
object JawbusPersonalBestAchievement : StageAchievement() {
    override val id: String = "jawbus_personal_best"
    override val name: String = "Jawbus Personal Best"
    override val description: String = "Reach a Lord Jawbus personal best kill time below 60/50/40/30/20 seconds."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.ISLE
    override val targetStage: Int = 5

    private val targetTimesMillis = longArrayOf(60_000L, 50_000L, 40_000L, 30_000L, 20_000L)

    init {
        addStageInfo(1, "Hot Pursuit", "Kill a Lord Jawbus you caught in under 1 minute.", AchievementDifficulty.EASY)
        addStageInfo(2, "Turning Up the Heat", "Kill a Lord Jawbus you caught in under 50 seconds.", AchievementDifficulty.EASY)
        addStageInfo(3, "Jawbreaker", "Kill a Lord Jawbus you caught in under 40 seconds.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Speed Demon", "Kill a Lord Jawbus you caught in under 30 seconds.", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Dethroned", "Kill a Lord Jawbus you caught in under 20 seconds.", AchievementDifficulty.HARD)
    }

    override fun setupListeners() {
        val savedBest = CatchTracker.catchHistory.catches.find { it.name == "Lord Jawbus" }?.bestKillTimeMs
        checkPersonalBest(savedBest)
        if (isCompleted) return

        activeListeners.add(registerSeaCreaturePersonalBestEvent { sc, durationMillis ->
            if (sc.scName == "Lord Jawbus") {
                checkPersonalBest(durationMillis)
            }
        })
    }

    private fun checkPersonalBest(durationMillis: Long?) {
        if (durationMillis == null || durationMillis <= 0L) return

        while (!isCompleted && durationMillis < targetTimesMillis[currentStage - 1]) {
            advanceStage()
        }
    }
}
