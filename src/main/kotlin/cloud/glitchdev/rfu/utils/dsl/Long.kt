package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.constants.skyblock.Skills
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** Set [precision] to 0 to preserve every digit in editable values. */
fun Long.compact(precision: Int = 3): String {
    val suffixes = arrayOf("", "k", "M", "B")
    val thousand = BigDecimal.valueOf(1_000)
    var value = BigDecimal.valueOf(this)
    var suffixIndex = 0
    while (value.abs() >= thousand && suffixIndex < suffixes.lastIndex) {
        value = value.movePointLeft(3)
        suffixIndex++
    }

    value = value.round(MathContext(precision, RoundingMode.HALF_UP))
    if (value.abs() >= thousand && suffixIndex < suffixes.lastIndex) {
        value = value.movePointLeft(3)
        suffixIndex++
    }
    return value.stripTrailingZeros().toPlainString() + suffixes[suffixIndex]
}

fun Int.compact(): String = this.toLong().compact()

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
