package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.Chat

@PartyCommand
object AllInviteCommand : AbstractPartyCommand(
    name = "allinv",
    description = "Toggles allinvite setting.",
    aliases = listOf("ai"),
    permission = listOf(PartyCommandPermission.LEADER_ONLY, PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleAllInviteCommand

    override fun execute(sender: String, args: List<String>) {
        Chat.sendCommand("p settings allinvite")
    }
}
