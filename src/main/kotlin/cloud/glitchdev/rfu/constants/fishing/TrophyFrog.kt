package cloud.glitchdev.rfu.constants.fishing

enum class TrophyFrog(override val displayName: String) : Trophy {
    COMMON_FROG("Common Frog"),
    LEAP_FROG("Leap Frog"),
    WETLANDS_FROG("Wetlands Frog"),
    REALITY_HOPPER("Reality Hopper"),
    EXPLODING_FROG("Exploding Frog"),
    SEA_FROG("Sea Frog"),
    BLESSED_FROG("Blessed Frog"),
    TREE_FROG("Tree Frog"),
    CAVE_FROG("Cave Frog"),
    HIGHLANDS_FROG("Highlands Frog"),
    BULLFROG("Bullfrog"),
    PUDDLE_JUMPER("Puddle Jumper");

    companion object {
        fun fromName(name: String): TrophyFrog? {
            val normalizedSearch = name.lowercase().replace("-", " ").trim()
            return entries.find {
                it.displayName.lowercase().replace("-", " ").trim() == normalizedSearch
            }
        }
    }
}
