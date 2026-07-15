package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.constants.text.TextColor

enum class DeployableType(
    val displayName: String,
    val labelColor: TextColor,
    val expiredTitle: String,
    val range: Double,
) {
    FLARE(
        displayName = "Flare",
        labelColor = TextColor.GOLD,
        expiredTitle = "§6§lFlare Expired!",
        range = 40.0,
    ),
    UMBERELLA(
        displayName = "Umberella",
        labelColor = TextColor.LIGHT_BLUE,
        expiredTitle = "§9§lUmberella Expired!",
        range = 30.0,
    ),
    FLUX(
        displayName = "Flux",
        labelColor = TextColor.MAGENTA,
        expiredTitle = "§d§lFlux Expired!",
        range = 18.0,
    );

    override fun toString(): String = displayName
}
