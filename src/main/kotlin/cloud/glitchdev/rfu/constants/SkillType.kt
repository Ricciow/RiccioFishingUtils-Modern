package cloud.glitchdev.rfu.constants

enum class SkillType(val displayName: String, val maxLevel: Int) {
    COMBAT("Combat", 60),
    FARMING("Farming", 60),
    FORAGING("Foraging", 54),
    FISHING("Fishing", 50),
    MINING("Mining", 60),
    ENCHANTING("Enchanting", 60),
    ALCHEMY("Alchemy", 50),
    CARPENTRY("Carpentry", 50),
    TAMING("Taming", 60),
    HUNTING("Hunting", 25);

    companion object {
        fun fromName(name: String): SkillType? {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}
