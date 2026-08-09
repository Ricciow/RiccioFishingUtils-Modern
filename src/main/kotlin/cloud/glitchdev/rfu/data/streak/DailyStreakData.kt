package cloud.glitchdev.rfu.data.streak

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    var currentProgress: Int = 0,
    val targetProgress: Int = 1,
    var isCompleted: Boolean = false
) {
    val progressPercent: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 1f
}

data class DailyStreakData(
    var currentStreak: Int = 0,
    var highestStreak: Int = 0,
    var totalChallengesCompleted: Int = 0,
    var totalDaysCompleted: Int = 0,
    var lastCompletedDate: String = "",
    var currentDate: String = "",
    var todayChallenges: List<DailyChallenge> = emptyList()
)
