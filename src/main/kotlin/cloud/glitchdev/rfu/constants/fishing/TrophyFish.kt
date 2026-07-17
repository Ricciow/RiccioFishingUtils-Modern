package cloud.glitchdev.rfu.constants.fishing

enum class TrophyFish(override val displayName: String) : Trophy {
    SLUGFISH("Slugfish"),
    FLYFISH("Flyfish"),
    VOLCANIC_STONEFISH("Volcanic Stonefish"),
    STEAMING_HOT_FLOUNDER("Steaming-Hot Flounder"),
    SULPHUR_SKITTER("Sulphur Skitter"),
    SKELETON_FISH("Skeleton Fish"),
    MOLDFIN("Moldfin"),
    SOULFISH("Soulfish"),
    BLOBFISH("Blobfish"),
    LAVAHORSE("Lavahorse"),
    VANILLE("Vanille"),
    KARATE_FISH("Karate Fish"),
    OBFUSCATED_1("Obfuscated 1"),
    OBFUSCATED_2("Obfuscated 2"),
    OBFUSCATED_3("Obfuscated 3"),
    GUSHER("Gusher"),
    MANA_RAY("Mana Ray"),
    GOLDEN_FISH("Golden Fish");

    companion object {
        fun fromName(name: String): TrophyFish? {
            val normalizedSearch = name.lowercase().replace("-", " ").trim()
            return entries.find {
                it.displayName.lowercase().replace("-", " ").trim() == normalizedSearch
            }
        }
    }
}
