package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.skyblock.Rarity

enum class TrophyFrog(
    override val displayName: String,
    override val rarity: Rarity = Rarity.COMMON,
    override val goldPity: Int = 100,
    override val diamondPity: Int = 600
) : Trophy {
    COMMON_FROG("Common Frog", Rarity.COMMON),
    LEAP_FROG("Leap Frog", Rarity.UNCOMMON),
    WETLANDS_FROG("Wetlands Frog", Rarity.UNCOMMON),
    REALITY_HOPPER("Reality Hopper", Rarity.UNCOMMON),
    EXPLODING_FROG("Exploding Frog", Rarity.UNCOMMON, 50, 300),
    SEA_FROG("Sea Frog", Rarity.RARE),
    BLESSED_FROG("Blessed Frog", Rarity.RARE),
    BULLFROG("Bullfrog", Rarity.RARE),
    TREE_FROG("Tree Frog", Rarity.EPIC),
    CAVE_FROG("Cave Frog", Rarity.EPIC),
    HIGHLANDS_FROG("Highlands Frog", Rarity.EPIC),
    PUDDLE_JUMPER("Puddle Jumper", Rarity.LEGENDARY, 50, 300);

    companion object {
        fun fromName(name: String): TrophyFrog? {
            val normalizedSearch = name.lowercase().replace("-", " ").trim()
            return entries.find {
                it.displayName.lowercase().replace("-", " ").trim() == normalizedSearch
            }
        }
    }
}
