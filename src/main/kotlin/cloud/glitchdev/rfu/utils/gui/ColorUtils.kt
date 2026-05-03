package cloud.glitchdev.rfu.utils.gui

import cloud.glitchdev.rfu.constants.text.TextColor
import java.awt.Color

object ColorUtils {
    fun TextColor.toJavaColor(): Color {
        return when (this) {
            TextColor.BLACK -> Color(0, 0, 0)
            TextColor.DARK_BLUE -> Color(0, 0, 170)
            TextColor.DARK_GREEN -> Color(0, 170, 0)
            TextColor.CYAN -> Color(0, 170, 170)
            TextColor.RED -> Color(170, 0, 0)
            TextColor.PURPLE -> Color(170, 0, 170)
            TextColor.GOLD -> Color(255, 170, 0)
            TextColor.GRAY -> Color(170, 170, 170)
            TextColor.DARK_GRAY -> Color(85, 85, 85)
            TextColor.LIGHT_BLUE -> Color(85, 85, 255)
            TextColor.LIGHT_GREEN -> Color(85, 255, 85)
            TextColor.AQUAMARINE -> Color(85, 255, 255)
            TextColor.LIGHT_RED -> Color(255, 85, 85)
            TextColor.MAGENTA -> Color(255, 85, 255)
            TextColor.YELLOW -> Color(255, 255, 85)
            TextColor.WHITE -> Color(255, 255, 255)
        }
    }
}
