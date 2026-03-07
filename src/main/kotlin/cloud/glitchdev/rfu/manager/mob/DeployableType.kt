package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.constants.text.TextColor

enum class DeployableType(
    val displayName: String,
    val labelColor: TextColor,
    val expiredTitle: String,
) {
    FLARE(
        displayName = "Flare",
        labelColor = TextColor.GOLD,
        expiredTitle = "§6§lFlare Expired!",
    ),
    UMBERELLA(
        displayName = "Umberella",
        labelColor = TextColor.LIGHT_BLUE,
        expiredTitle = "§9§lUmberella Expired!",
    );

    override fun toString(): String = displayName
}
