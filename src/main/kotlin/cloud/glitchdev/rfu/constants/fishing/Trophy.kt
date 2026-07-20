package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.skyblock.Rarity

interface Trophy {
    val displayName: String
    val name: String
    val rarity: Rarity
    val goldPity: Int
    val diamondPity: Int
}
