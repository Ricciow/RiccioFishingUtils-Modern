package cloud.glitchdev.rfu.constants.fishing

enum class HotspotShareChat(val command: String, private val displayName: String) {
    PARTY("pc", "Party"),
    ALL_CHAT("ac", "All Chat");

    override fun toString(): String = displayName
}
