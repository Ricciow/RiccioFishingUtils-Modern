package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings

import cloud.glitchdev.rfu.utils.User

@PartyCommand
object CoordsCommand : AbstractPartyCommand(
    name = "coords",
    description = "Sends your current coordinates to the party.",
    aliases = listOf("c", "xyz"),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleCoordsCommand

    override fun execute(sender: String, args: List<String>) {
        if (args.isNotEmpty()) {
            val targetUser = args[0]
            val myUsername = User.getUsername()
            if (!myUsername.contains(targetUser, ignoreCase = true)) {
                return
            }
        }

        val player = mc.player ?: return
        val pos = player.blockPosition()
        sendPartyMessage("X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}")
    }
}
