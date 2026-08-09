package cloud.glitchdev.rfu.feature.streak.challenge

import cloud.glitchdev.rfu.constants.text.TextColor

enum class ChallengeLevel(val displayName: String, val color: TextColor) {
    BASIC("Basic", TextColor.LIGHT_GREEN),
    INTERMEDIATE("Intermediate", TextColor.YELLOW),
    ADVANCED("Advanced", TextColor.LIGHT_RED),
    ELITE("Elite", TextColor.MAGENTA);

    val formattedName: String
        get() = "$color$displayName"
}
