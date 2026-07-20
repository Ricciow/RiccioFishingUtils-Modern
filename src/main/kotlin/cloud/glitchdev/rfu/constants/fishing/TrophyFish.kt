package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.skyblock.Rarity

enum class TrophyFish(
    override val displayName: String,
    override val rarity: Rarity = Rarity.COMMON,
    override val goldPity: Int = 100,
    override val diamondPity: Int = 600
) : Trophy {
    STEAMING_HOT_FLOUNDER("Steaming-Hot Flounder", Rarity.COMMON),
    SULPHUR_SKITTER("Sulphur Skitter", Rarity.COMMON),
    BLOBFISH("Blobfish", Rarity.COMMON),
    GUSHER("Gusher", Rarity.COMMON),
    OBFUSCATED_1("Obfuscated 1", Rarity.COMMON),
    SLUGFISH("Slugfish", Rarity.UNCOMMON),
    FLYFISH("Flyfish", Rarity.UNCOMMON),
    OBFUSCATED_2("Obfuscated 2", Rarity.UNCOMMON),
    VOLCANIC_STONEFISH("Volcanic Stonefish", Rarity.RARE),
    LAVAHORSE("Lavahorse", Rarity.RARE),
    VANILLE("Vanille", Rarity.RARE),
    MANA_RAY("Mana Ray", Rarity.RARE),
    OBFUSCATED_3("Obfuscated 3", Rarity.RARE),
    SKELETON_FISH("Skeleton Fish", Rarity.RARE),
    MOLDFIN("Moldfin", Rarity.EPIC),
    SOULFISH("Soulfish", Rarity.EPIC),
    KARATE_FISH("Karate Fish", Rarity.EPIC),
    GOLDEN_FISH("Golden Fish", Rarity.LEGENDARY);

    companion object {
        fun fromName(name: String): TrophyFish? {
            val normalizedSearch = name.lowercase().replace("-", " ").trim()
            return entries.find {
                it.displayName.lowercase().replace("-", " ").trim() == normalizedSearch
            }
        }
    }
}
