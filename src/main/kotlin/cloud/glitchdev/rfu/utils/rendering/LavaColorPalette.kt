package cloud.glitchdev.rfu.utils.rendering

import kotlin.math.roundToInt

data class LavaColorPalette(val dark: Int, val midtone: Int, val highlight: Int) {
    fun recolor(pixel: Int, minimumBrightness: Int, maximumBrightness: Int): Int {
        val brightness = brightness(pixel)
        val position = if (maximumBrightness > minimumBrightness) {
            ((brightness - minimumBrightness).toFloat() / (maximumBrightness - minimumBrightness)).coerceIn(0f, 1f)
        } else 0.5f
        val first = if (position <= 0.5f) dark else midtone
        val second = if (position <= 0.5f) midtone else highlight
        val blend = if (position <= 0.5f) position * 2f else (position - 0.5f) * 2f

        fun channel(shift: Int): Int {
            val start = (first ushr shift) and 255
            val end = (second ushr shift) and 255
            return (start + (end - start) * blend).roundToInt() shl shift
        }

        return (pixel and 0xFF000000.toInt()) or channel(16) or channel(8) or channel(0)
    }

    companion object {
        fun brightness(pixel: Int): Int =
            ((pixel ushr 16 and 255) * 54 + (pixel ushr 8 and 255) * 183 + (pixel and 255) * 19) / 256
    }
}
