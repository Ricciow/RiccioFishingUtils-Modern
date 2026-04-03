package cloud.glitchdev.rfu.gui

import java.awt.Color

object UIScheme {
    val primaryColor = Color(37, 114, 153)
    val primaryColorOpaque = increaseOpacity(primaryColor, 127)

    val secondaryColor = Color(200, 200, 200)
    val secondaryColorOpaque = increaseOpacity(secondaryColor, 127)

    val secondaryColorDisabled = Color(150, 150, 150)
    val secondaryColorDisabledOpaque = increaseOpacity(secondaryColorDisabled, 127)

    val primaryTextColor = Color(255, 255, 255)
    val secondaryTextColor = Color(180, 180, 180)

    val denyColor = Color(209, 23, 23)
    val allowColor = Color(23, 209, 51)

    val darkBackground = Color(0, 0, 0, 128)
    val transparent = Color(0, 0, 0, 0)

    val barHighHP = Color(85, 255, 85)
    val barMediumHP = Color(255, 255, 85)
    val barLowHP = Color(255, 85, 85)
    val barShuriken = Color(85, 255, 255)

    val diedColor = Color(120, 7, 7)

    val errorPopupColor = Color(255, 85, 85)

    val trackedStarColor = Color(255, 215, 0)
    val untrackedStarColor = Color(160, 160, 160)

    val achievementBgColor = Color(10, 40, 50)
    val achievementDescriptionColor = Color(200, 200, 200)
    val achievementBgColorOpaque = increaseOpacity(achievementBgColor, 127)
    val achievementCompleteColor = Color(85, 255, 85)
    val achievementIncompleteColor = Color(255, 255, 85)
    val easyDifficultyColor = Color(85, 255, 85)
    val mediumDifficultyColor = Color(255, 255, 85)
    val hardDifficultyColor = Color(255, 85, 85)
    val veryHardDifficultyColor = Color(120, 7, 7)
    val impossibleDifficultyColor = Color(90, 0, 0)

    const val HOVER_EFFECT_DURATION = 0.1f

    fun increaseOpacity(baseColor : Color, amount: Int) : Color {
        return Color(baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha - amount)
    }

    fun decreaseOpacity(baseColor: Color, amount: Int) : Color {
        return Color(baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha + amount)
    }
}