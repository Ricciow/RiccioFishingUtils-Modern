package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings

@PartyCommand
object CoordsCommand : AbstractPartyCommand(
    name = "coords",
    description = "Sends your current coordinates to the party.",
    aliases = listOf("c", "xyz"),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleCoordsCommand

    override fun execute(sender: String, args: List<String>) {
        val player = mc.player ?: return
        val pos = player.blockPosition()
        sendPartyMessage("X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}")
    }
}
