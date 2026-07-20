package cloud.glitchdev.rfu.data.fishing

data class TrophyData(
    val pity: TrophyPityData = TrophyPityData(),
    var hasSynced: Boolean = false
)
