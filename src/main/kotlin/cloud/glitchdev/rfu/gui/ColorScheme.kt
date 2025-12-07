package cloud.glitchdev.rfu.gui

import java.awt.Color

object ColorScheme {
    val primaryColor = Color(37, 114, 153)
    val primaryColorOpaque = increaseOpacity(primaryColor, 127)

    val secondaryColor = Color(200, 200, 200)
    val secondaryColorOpaque = increaseOpacity(secondaryColor, 127)

    val primaryTextColor = Color(255, 255, 255)
    val secondaryTextColor = Color(180, 180, 180)

    fun increaseOpacity(baseColor : Color, amount: Int) : Color {
        return Color(baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha - amount)
    }

    fun decreaseOpacity(baseColor: Color, amount: Int) : Color {
        return Color(baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha + amount)
    }
}