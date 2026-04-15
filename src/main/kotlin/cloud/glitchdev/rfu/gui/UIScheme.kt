package cloud.glitchdev.rfu.gui

import cloud.glitchdev.rfu.constants.FishingIslands
import java.awt.Color

object UIScheme {
    val primaryColor = Color(37, 114, 153)
    val primaryColorOpaque = primaryColor.increaseOpacity(127)

    val secondaryColor = Color(200, 200, 200)
    val secondaryColorOpaque = secondaryColor.increaseOpacity(127)

    val secondaryColorDisabled = Color(150, 150, 150)
    val secondaryColorDisabledOpaque = secondaryColorDisabled.increaseOpacity(127)

    val primaryTextColor = Color(255, 255, 255)
    val secondaryTextColor = Color(180, 180, 180)

    val denyColor = Color(209, 23, 23)
    val allowColor = Color(23, 209, 51)

    val darkBackground = Color(0, 0, 0, 128)
    val transparent = Color(0, 0, 0, 0)

    //Boss Bar
    val barHighHP = Color(85, 255, 85)
    val barMediumHP = Color(255, 255, 85)
    val barLowHP = Color(255, 85, 85)
    val barShuriken = Color(85, 255, 255)

    //Died to jawbus
    val diedColor = Color(120, 7, 7)

    //Error Popup text
    val errorPopupColor = Color(255, 85, 85)

    //Achievements
    val trackedStarColor = Color(255, 215, 0)
    val untrackedStarColor = Color(160, 160, 160)
    val achievementBgColor = Color(10, 40, 50)
    val achievementDescriptionColor = Color(200, 200, 200)
    val achievementBgColorOpaque = achievementBgColor.increaseOpacity(127)
    val achievementCompleteColor = Color(85, 255, 85)
    val achievementIncompleteColor = Color(255, 255, 85)
    val easyDifficultyColor = Color(85, 255, 85)
    val mediumDifficultyColor = Color(255, 255, 85)
    val hardDifficultyColor = Color(255, 85, 85)
    val veryHardDifficultyColor = Color(120, 7, 7)
    val impossibleDifficultyColor = Color(90, 0, 0)

    //Sea Creatures Window
    val windowBackground = Color(30, 30, 30, 220)
    val sidebarBackground = Color(20, 20, 20, 180)
    val contentBackground = Color(40, 40, 40, 150)
    val selectedTextColor = Color.YELLOW

    //Party Finder Window
    val pfWindowBackground = Color(30, 30, 30, 220)
    val pfWindowSeparator = Color(45, 75, 200)
    val pfTitleText = pfWindowSeparator
    val pfInputBg = Color(100, 100, 100, 200)
    val pfInputBgHovered = Color(45, 75, 200, 100)
    val pfDropdownSelected = Color(45, 75, 200, 200)
    val pfScrollBar = pfWindowSeparator
    //Pf card
    val pfCardBg = pfWindowBackground
    val pfCardBorderWidth = 1.5f
    val pfCardInnerPadding = 5f
    val pfCardSmallPadding = 3f
    val pfCardBorder = Color(100, 100, 100, 100)
    val pfCardBorderHovered = Color(45, 75, 200, 100)
    val pfCardUserColor = Color(100, 100, 100)
    val pfCardTitleColor = Color.WHITE
    val pfCardTitleHoverColor = Color(45, 75, 200)
    val pfCardSeparator = Color(100, 100, 100)
    val pfCardSeparatorHover = pfWindowSeparator
    val pfCardLevelBorderColor = Color(100, 100, 100)
    val pfCardLevelBorderHoveredColor = pfWindowSeparator
    val pfCardLevelBgColor = pfWindowBackground
    val pfCardLevelLabelColor = Color(180, 180, 180)
    val pfCardDescriptionColor = Color(150, 150, 150)
    //ConditionCard
    val pfConditionCardWidth = 1f
    val pfConditionCardPadding = 2f
    val pfConditionCardWater = Color(42, 42, 128)
    val pfConditionCardWaterBorder = Color(85, 85, 255)
    val pfConditionCardLooting5 = Color(42, 128, 128)
    val pfConditionCardLooting5Border = Color(85, 255, 255)
    val pfConditionCardLava = Color(128, 85, 0)
    val pfConditionCardLavaBorder = Color(255, 170, 0)
    val pfConditionCardKiller = Color(128, 42, 42)
    val pfConditionCardKillerBorder = Color(255, 85, 85)
    val pfConditionCardEnderman9 = Color(85, 0, 85)
    val pfConditionCardEnderman9Border = Color(170, 0, 170)
    val pfConditionCardBrainFood = Color(128, 42, 128)
    val pfConditionCardBrainFoodBorder = Color(255, 85, 255)
    val pfConditionCardIsland = Color(45, 45, 45)
    val pfConditionCardIslandBorder = Color(100, 100, 100)

    fun getIslandColor(island: String): Color {
        val islandObj = FishingIslands.findIslandObject(island)
        val baseColor = islandObj?.color ?: pfConditionCardIsland
        return Color(baseColor.red / 3, baseColor.green / 3, baseColor.blue / 3)
    }

    fun getIslandBorderColor(island: String): Color {
        val islandObj = FishingIslands.findIslandObject(island)
        return islandObj?.color ?: pfConditionCardIslandBorder
    }

    val pfConditionCardUnknown = Color(100, 100, 100)
    val pfConditionCardUnknownBorder = Color(200, 200, 200)

    const val HOVER_EFFECT_DURATION = 0.1f

    fun Color.increaseOpacity(amount: Int): Color {
        return Color(red, green, blue, maxOf(alpha - amount, 0))
    }
    
    fun Color.decreaseOpacity(amount: Int): Color {
        return Color(red, green, blue, minOf(alpha + amount, 255))
    }
}