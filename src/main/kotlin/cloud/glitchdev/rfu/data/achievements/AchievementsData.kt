package cloud.glitchdev.rfu.data.achievements

data class AchievementsData(
    var achievements: MutableMap<String, AchievementSaveData> = mutableMapOf()
) {
    data class AchievementSaveData(
        val id: String,
        val isCompleted: Boolean,
        val isCheated: Boolean = false,
        val progressData: Map<String, Any> = emptyMap()
    )
}