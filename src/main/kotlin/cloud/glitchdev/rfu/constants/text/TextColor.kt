package cloud.glitchdev.rfu.constants.text

enum class TextColor(val code : String) {
    BLACK("§0"),
    DARK_BLUE("§1"),
    DARK_GREEN("§2"),
    CYAN("§3"),
    RED("§4"),
    PURPLE("§5"),
    GOLD("§6"),
    GRAY("§7"),
    DARK_GRAY("§8"),
    LIGHT_BLUE("§9"),
    LIGHT_GREEN("§a"),
    AQUAMARINE("§b"),
    LIGHT_RED("§c"),
    MAGENTA("§d"),
    YELLOW("§e"),
    WHITE("§f");

    override fun toString(): String {
        return this.code
    }
}