package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.party.WarpKickManager
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.Party

@PartyCommand
object WarpCommand : AbstractPartyCommand(
    name = "warp",
    description = "Warps the party.",
    aliases = listOf("w"),
    responseTemplates = listOf(
        "No need to warp. (Togglewarp ON)" to "&cNo need to warp. (Togglewarp ON)"
    ),
    permission = listOf(PartyCommandPermission.LEADER_ONLY, PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleWarpCommand

    override fun execute(sender: String, args: List<String>) {
        if (Party.members.size == 2 && Party.members.keys.any { WarpKickManager.isUserOnList(it) }) {
            sendPartyMessage("No need to warp. (Togglewarp ON)")
            return
        }
        WarpKickManager.executeWarpWithKicks()
    }
}