package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.utils.Chat

@PartyCommand
object WarpCommand : AbstractPartyCommand(
    name = "warp",
    description = "Warps the party.",
    aliases = listOf("w")
) {
    override fun execute(sender: String, args: List<String>) {
        Chat.sendCommand("p warp")
    }
}