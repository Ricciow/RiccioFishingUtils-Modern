package cloud.glitchdev.rfu.data.fishing

data class TrophyData(
    val pity: TrophyPityData = TrophyPityData(),
    val count: TrophyCountData = TrophyCountData()
)
