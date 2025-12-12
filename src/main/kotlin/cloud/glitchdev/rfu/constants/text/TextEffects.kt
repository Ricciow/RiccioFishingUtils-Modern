package cloud.glitchdev.rfu.constants.text

enum class TextEffects (val code : String) {
    BOLD("§l"),
    UNDERLINE("§n"),
    ITALIC("§o"),
    MAGIC("§k"),
    STRIKE("§m"),
    RESET("§r");

    override fun toString(): String {
        return this.code
    }
}