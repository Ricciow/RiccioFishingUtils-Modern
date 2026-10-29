package cloud.glitchdev.rfu.utils.rendering

import kotlin.math.roundToInt

class LiquidTexturePixels(private val original: IntArray, private val vanillaAlpha: Int) {
    private val minimum: Int
    private val maximum: Int

    init {
        var darkest = 255
        var brightest = 0
        for (pixel in original) {
            if (pixel ushr 24 == 0) continue
            val brightness = LavaColorPalette.brightness(pixel)
            darkest = minOf(darkest, brightness)
            brightest = maxOf(brightest, brightness)
        }
        minimum = darkest
        maximum = brightest
    }

    fun recolor(palette: LavaColorPalette?, alpha: Int): IntArray = IntArray(original.size) { index ->
        val pixel = original[index]
        val color = if (palette != null && pixel ushr 24 != 0) palette.recolor(pixel, minimum, maximum) else pixel
        val opacity = ((pixel ushr 24) * alpha.toFloat() / vanillaAlpha).roundToInt().coerceIn(0, 255)
        (color and 0xFFFFFF) or (opacity shl 24)
    }
}