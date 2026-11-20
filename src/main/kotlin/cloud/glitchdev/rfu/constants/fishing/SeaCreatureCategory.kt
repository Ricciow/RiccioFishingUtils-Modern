package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.config.seacreatures.SeaCreatureSettingsManager

data class SeaCreatureCategory(val id: String) {
    val displayName: String
        get() = SeaCreatureSettingsManager.getCategory(id)?.displayName ?: id

    val islands: List<FishingIslands>
        get() = SeaCreatureSettingsManager.getCategory(id)?.islands.orEmpty()

    companion object {
        const val GENERAL_WATER = "GENERAL_WATER"
        const val SPOOKY = "SPOOKY"
        const val SHARK = "SHARK"
        const val WINTER = "WINTER"
    }
}
