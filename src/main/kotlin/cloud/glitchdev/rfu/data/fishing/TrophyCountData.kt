package cloud.glitchdev.rfu.data.fishing

data class TrophyCountData(
    var hasSyncedFrogs: Boolean = false,
    var hasSyncedFish: Boolean = false,
    val frogCount: MutableMap<String, TrophyCountEntry> = mutableMapOf(),
    val fishCount: MutableMap<String, TrophyCountEntry> = mutableMapOf()
)
