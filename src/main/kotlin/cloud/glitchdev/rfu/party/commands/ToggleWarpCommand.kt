package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.WarpKickManager
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils

@PartyCommand
object ToggleWarpCommand : AbstractPartyCommand(
    name = "togglewarp",
    description = "Toggles whether you should be temporarily kicked during a warp.",
    aliases = listOf("tw"),
    responseTemplates = listOf(
        "You will no longer be warped." to "&aYou will no longer be warped.",
        "You will be warped now." to "&aYou will be warped now."
    ),
    permission = emptyList()
) {
    override fun isEnabled() = PartySettings.toggleToggleWarpCommand

    override fun execute(sender: String, args: List<String>) {
        val added = WarpKickManager.toggleUser(sender)
        val message = if (added) {
            "You will no longer be warped."
        } else {
            "You will be warped now."
        }
        sendPartyMessage(message)
    }
}
