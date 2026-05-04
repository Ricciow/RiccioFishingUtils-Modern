package cloud.glitchdev.rfu.model.pets

import cloud.glitchdev.rfu.constants.text.TextColor

enum class PetCategory(val color : TextColor) {
    ALCHEMY(TextColor.GOLD),
    COMBAT(TextColor.LIGHT_RED),
    ENCHANTING(TextColor.AQUAMARINE),
    FARMING(TextColor.YELLOW),
    FISHING(TextColor.LIGHT_BLUE),
    FORAGING(TextColor.DARK_GREEN),
    MINING(TextColor.GRAY),
    TAMING(TextColor.MAGENTA),
    OTHER(TextColor.PURPLE);

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
