package cloud.glitchdev.rfu.utils.dsl

import kotlin.math.roundToInt

fun Float.roundToDecimal(): Float {
    return (this * 10f).roundToInt() / 10f
}