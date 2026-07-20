package cloud.glitchdev.rfu.data.fishing

data class TrophyPityData(
    val fishPity: MutableMap<String, TrophyPityEntry> = mutableMapOf(),
    val frogPity: MutableMap<String, TrophyPityEntry> = mutableMapOf()
)
