package cloud.glitchdev.rfu.constants.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.RodTimerCustom
import cloud.glitchdev.rfu.constants.ui.TextAlignment

enum class RodTimerStyle(val displayName: String, private val formatProvider: () -> RodTimerFormat) {
    DEFAULT("Default", { RodTimerFormat("&e&l{time}", "&c&l${GeneralFishing.rodTimerReadyText}") }),
    GREATCATCH_1("GreatCatch 1", {
        RodTimerFormat(
            countdownText = "&b🎣 &a[{bar}&a]&b α",
            readyText = "&b🎣 🎣 🎣 &a&lREEL IN! &b🎣 🎣 🎣",
            barSymbol = ":",
            barLength = 50,
            filledColor = "&a",
            remainingColor = "&c"
        )
    }),
    GREATCATCH_2("GreatCatch 2", {
        RodTimerFormat(
            countdownText = "&b&l🎣 {bar}&r &b&lα",
            readyText = "&b🎣 🎣 🎣 &a&lREEL IN! &b🎣 🎣 🎣",
            barSymbol = "  ",
            barLength = 13,
            shrinkingBar = true,
            remainingColor = "&f&m",
            alignment = TextAlignment.LEFT
        )
    }),
    CUSTOM("Custom", { RodTimerCustom.toFormat() });

    val format: RodTimerFormat
        get() = formatProvider()

    override fun toString(): String = displayName
}