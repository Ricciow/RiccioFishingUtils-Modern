package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.events.managers.SeaCreaturePersonalBestEvents.registerSeaCreaturePersonalBestEvent

@Achievement
object ThunderPersonalBestAchievement : StageAchievement() {
    override val id: String = "thunder_personal_best"
    override val name: String = "Thunder Personal Best"
    override val description: String = "Reach a Thunder personal best kill time below 30/15/10/5/1 seconds."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE
    override val targetStage: Int = 5

    private val targetTimesMillis = longArrayOf(30_000L, 15_000L, 10_000L, 5_000L, 1_000L)

    init {
        addStageInfo(1, "Storm Chaser", "Kill a Thunder you caught in under 30 seconds.", AchievementDifficulty.EASY)
        addStageInfo(2, "Thunderstruck", "Kill a Thunder you caught in under 15 seconds.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Gone in a Flash", "Kill a Thunder you caught in under 10 seconds.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Jump cut", "Kill a Thunder you caught in under 5 seconds.", AchievementDifficulty.HARD)
        addStageInfo(5, "Optimal", "Kill a Thunder you caught in under 1 second.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        val savedBest = CatchTracker.catchHistory.catches.find { it.name == "Thunder" }?.bestKillTimeMs
        checkPersonalBest(savedBest)
        if (isCompleted) return

        activeListeners.add(registerSeaCreaturePersonalBestEvent { sc, durationMillis ->
            if (sc.scName == "Thunder") {
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
