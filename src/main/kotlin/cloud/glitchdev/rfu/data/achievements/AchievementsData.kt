package cloud.glitchdev.rfu.data.achievements

data class AchievementsData(
    var version: Int = 0,
    var achievements: MutableMap<String, AchievementSaveData> = mutableMapOf(),
    var trackedAchievements: MutableSet<String> = mutableSetOf()
) {
    data class AchievementSaveData(
        val id: String,
        val isCompleted: Boolean,
        val isCheated: Boolean = false,
        val progressData: Map<String, Any> = emptyMap()
    )
}