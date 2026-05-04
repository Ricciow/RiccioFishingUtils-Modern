package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party

@PartyCommand
object TransferCommand : AbstractPartyCommand(
    name = "transfer",
    description = "Transfers the party to another user.",
    aliases = listOf("pt", "ptme"),
    permission = listOf(PartyCommandPermission.LEADER_ONLY, PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleTransferCommand

    override fun execute(sender: String, args: List<String>) {
        val target = if (args.isNotEmpty()) args[0] else sender

        val transfer = Party.members.keys.find { it.contains(target) } ?: target

        Chat.sendCommand("p transfer $transfer")
    }
}
