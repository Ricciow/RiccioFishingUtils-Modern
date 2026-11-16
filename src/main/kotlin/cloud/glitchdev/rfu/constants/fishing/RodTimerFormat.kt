package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.constants.ui.TextAlignment
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import kotlin.math.ceil
import kotlin.math.roundToInt

data class RodTimerFormat(
    val countdownText: String,
    val readyText: String,
    val barSymbol: String = "",
    val barLength: Int = 0,
    val shrinkingBar: Boolean = false,
    val filledColor: String = "",
    val remainingColor: String = "",
    val alignment: TextAlignment = TextAlignment.CENTER
) {
    fun ready(): String = readyText.toDisplay()

    fun countdown(time: Float, remaining: Float): String = countdownText
        .replace("{time}", time.toString())
        .replace("{bar}", bar(remaining))
        .toDisplay()

    private fun String.toDisplay(): String = replace("\\n", "\n").toMcCodes()

    private fun bar(remaining: Float): String {
        if (shrinkingBar) {
            val count = ceil(remaining * barLength - 0.001f).toInt().coerceIn(0, barLength)
            return remainingColor + barSymbol.repeat(count)
        }

        val filled = ((1f - remaining) * barLength).roundToInt().coerceIn(0, barLength)
        return filledColor + barSymbol.repeat(filled) + remainingColor + barSymbol.repeat(barLength - filled)
    }
}