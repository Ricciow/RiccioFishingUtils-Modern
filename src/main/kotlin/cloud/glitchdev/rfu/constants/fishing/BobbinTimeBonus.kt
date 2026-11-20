package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.text.TextColor
import kotlin.math.roundToInt

enum class BobbinTimeBonus(val displayName: String, val color: TextColor, val suffix: String) {
    PERCENTAGE("Buffs", TextColor.LIGHT_BLUE, "%"),
    MAGIC_FIND("Magic Find", TextColor.AQUAMARINE, "\uE01A"),
    TRACKING("Tracking", TextColor.MAGENTA, "\uE077");

    override fun toString(): String = displayName

    companion object {
        fun formatAmount(buffPercentage: Double): String {
            val tenths = (buffPercentage * 1000).roundToInt()
            return if (tenths % 10 == 0) (tenths / 10).toString() else (tenths / 10.0).toString()
        }
    }
}
