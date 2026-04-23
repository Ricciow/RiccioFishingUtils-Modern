package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.text.TextColor

enum class Rarity(val color: TextColor) {
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
    ADMIN(TextColor.RED)
}