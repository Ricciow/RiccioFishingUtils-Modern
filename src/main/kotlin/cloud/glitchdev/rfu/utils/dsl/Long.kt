package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.constants.skyblock.Skills
import java.util.Locale

fun Long.compact(): String {
    return when {
        this >= 1_000_000_000L -> formatDecimal(this / 1_000_000_000.0) + "B"
        this >= 1_000_000L -> formatDecimal(this / 1_000_000.0) + "M"
        this >= 10_000L -> formatDecimal(this / 1_000.0) + "k"
        else -> String.format(Locale.US, "%,d", this)
    }
}

private fun formatDecimal(value: Double): String {
    val s = String.format(Locale.US, "%.1f", value)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}

fun Long.toSkillLevel(): Int {
    if (this >= Skills.TOTAL_XP_MAX_LEVEL) {
        var level = 60
        var xpCurrent = this - Skills.TOTAL_XP_MAX_LEVEL
        var slope = 600_000L
        var xpForCurr = 7_000_000L + slope
        while (xpCurrent >= xpForCurr) {
            level++
            xpCurrent -= xpForCurr
            xpForCurr += slope
            if (level % 10 == 0) slope *= 2
        }
        return level
    }

    val index = Skills.TOTAL_XP_ACCUMULATED_PREVIOUS_LEVEL.binarySearch(this)

    return if (index >= 0) {
        index
    } else {
        -(index + 1) - 1
    }
}