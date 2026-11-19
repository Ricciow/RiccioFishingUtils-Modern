package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.text.TextColor

enum class BobbinTimeBonus(val displayName: String, val color: TextColor, val suffix: String) {
    PERCENTAGE("Buffs", TextColor.LIGHT_BLUE, "%"),
    MAGIC_FIND("Magic Find", TextColor.AQUAMARINE, "\uE01A"),
    TRACKING("Tracking", TextColor.MAGENTA, "\uE077");

    override fun toString(): String = displayName
}
