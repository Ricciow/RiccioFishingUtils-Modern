package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.RiccioFishingUtils.RFU_VERSION
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand

@PartyCommand
object VersionCommand : AbstractPartyCommand(
    name = "version",
    description = "Shows the current RFU version.",
    responseTemplates = listOf(
        "RFU version: {version}" to "&9&l{sender}: &e{1}"
    )
) {
    override fun execute(sender: String, args: List<String>) {
        sendPartyMessage(formatResponse(responseTemplates[0].first, "version" to RFU_VERSION.friendlyString))
    }
}
