package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.constants.fishing.RodTimerFormat
import cloud.glitchdev.rfu.constants.ui.TextAlignment
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt

object RodTimerCustom : ObjectKt() {
    var countdownText by string("&b🎣 &a[{bar}&a] &e&l{time}") {
        name = Literal("Countdown Text")
        description = Literal("Text shown while the timer is counting down. Placeholders: {time} = seconds left, {bar} = the bar configured below.")
    }

    var readyText by string("&a&lREEL IN!") {
        name = Literal("Ready Text")
        description = Literal("Text shown when the rod timer ends.")
    }

    var barSymbol by string("|") {
        name = Literal("Bar Symbol")
        description = Literal("The text used to build the {bar}.")
    }

    var barLength by int(25) {
        name = Literal("Bar Length")
        description = Literal("How many symbols the {bar} has.")
        range = 1..100
        slider = true
    }

    var shrinkingBar by boolean(false) {
        name = Literal("Shrinking Bar")
        description = Literal("Makes the {bar} lose symbols as the timer runs out, instead of filling up with color.")
    }

    var filledColor by string("&a") {
        name = Literal("Bar Filled Color")
        description = Literal("Codes for the part of the {bar} that is already filled. Not used by the shrinking bar.")
    }

    var remainingColor by string("&c") {
        name = Literal("Bar Remaining Color")
        description = Literal("Color of the part of the {bar} that is still left")
    }

    var alignment by enum(TextAlignment.CENTER) {
        name = Literal("Alignment")
        description = Literal("Which side of the timer stays still as it changes size.")
    }

    fun toFormat() = RodTimerFormat(countdownText, readyText, barSymbol, barLength, shrinkingBar, filledColor, remainingColor, alignment)
}