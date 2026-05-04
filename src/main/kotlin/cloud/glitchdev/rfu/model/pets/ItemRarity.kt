package cloud.glitchdev.rfu.model.pets

import cloud.glitchdev.rfu.constants.text.TextColor

enum class ItemRarity(val color: TextColor) {
    COMMON(TextColor.WHITE),
    UNCOMMON(TextColor.LIGHT_GREEN),
    RARE(TextColor.LIGHT_BLUE),
    EPIC(TextColor.PURPLE),
    LEGENDARY(TextColor.GOLD),
    MYTHIC(TextColor.MAGENTA),
    DIVINE(TextColor.AQUAMARINE),
    SPECIAL(TextColor.LIGHT_RED),
    VERY_SPECIAL(TextColor.LIGHT_RED),
    ULTIMATE(TextColor.RED),
    SUPREME(TextColor.RED),
    ADMIN(TextColor.RED);

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}
