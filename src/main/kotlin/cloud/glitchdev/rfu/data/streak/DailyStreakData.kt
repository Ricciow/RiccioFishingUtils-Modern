package cloud.glitchdev.rfu.data.streak

import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry

data class DailyChallenge(
    val id: String,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false
) {
    val baseDef: BaseChallenge?
        get() = ChallengeRegistry.getChallenge(id)

    fun getTitle(streakDays: Int = DailyStreakManager.data.currentStreak): String =
        baseDef?.getTitle(streakDays) ?: ""

    fun getDescription(streakDays: Int = DailyStreakManager.data.currentStreak): String =
        baseDef?.getDescription(streakDays) ?: ""

    fun getLevel(streakDays: Int = DailyStreakManager.data.currentStreak): ChallengeLevel =
        baseDef?.getLevel(streakDays) ?: ChallengeLevel.BASIC

    fun getTargetProgress(streakDays: Int = DailyStreakManager.data.currentStreak): Int =
        baseDef?.getTargetProgress(streakDays) ?: 1

    fun getProgressPercent(streakDays: Int = DailyStreakManager.data.currentStreak): Float {
        val target = getTargetProgress(streakDays)
        return if (target > 0) (currentProgress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 1f
    }
}

data class DailyStreakData(
    var currentStreak: Int = 0,
    var highestStreak: Int = 0,
    var totalChallengesCompleted: Int = 0,
    var totalDaysCompleted: Int = 0,
    var lastCompletedDate: String = "",
    var currentDate: String = "",
    var hasRerolledToday: Boolean = false,
    var todayChallenges: List<DailyChallenge> = emptyList()
)
