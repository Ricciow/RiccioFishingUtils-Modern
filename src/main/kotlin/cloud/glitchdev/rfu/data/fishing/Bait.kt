package cloud.glitchdev.rfu.data.fishing

enum class Bait(val displayName: String) {
    MINNOW("Minnow Bait"),
    DARK("Dark Bait"),
    SPOOKY("Spooky Bait"),
    LIGHT("Light Bait"),
    SPIKED("Spiked Bait"),
    FISH("Fish Bait"),
    CARROT("Carrot Bait"),
    CORRUPTED("Corrupted Bait"),
    OBFUSCATED_1("Obfuscated 1"),
    OBFUSCATED_2("Obfuscated 2"),
    ICE("Ice Bait"),
    BLESSED("Blessed Bait"),
    SHARK("Shark Bait"),
    GLOWY_CHUM("Glowy Chum Bait"),
    HOT("Hot Bait"),
    WORM("Worm Bait"),
    GOLDEN("Golden Bait"),
    WHALE("Whale Bait"),
    FROZEN("Frozen Bait"),
    HOTSPOT("Hotspot Bait"),
    TREASURE("Treasure Bait");

    companion object {
        fun fromName(name: String): Bait? {
            return entries.find { name.contains(it.displayName, ignoreCase = true) }
        }
    }
}
