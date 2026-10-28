package cloud.glitchdev.rfu.constants.chat

enum class PartyCommandListMode {
    BLACKLIST,
    WHITELIST;

    override fun toString(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}
