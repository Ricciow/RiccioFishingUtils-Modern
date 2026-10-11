package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.compact
import cloud.glitchdev.rfu.utils.dsl.toReadableString

@PartyCommand
object XphCommand : AbstractPartyCommand(
    name = "xph",
    description = "Shows your current and average Fishing XP per hour.",
    aliases = listOf("xpph"),
    responseTemplates = listOf(
        "Current XP/h: {current} | Average: {avg} | Timer: {time}" to "&9&l{sender} &b- &6XP/h&b:\n &f{1} &eCurrent &7| &f{2} &eAverage &7| &eTimer: &f{3}"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleXphCommand

    override fun execute(sender: String, args: List<String>) {
        if (!FishingSession.isFishing) return

        if (args.isNotEmpty()) {
            val targetUser = args[0]
            val myUsername = User.getUsername()
            if (!targetUser.equals(myUsername, ignoreCase = true)) {
                return
            }
        }

        val xpTracker = FishingSession.xpTracker
        val response = formatResponse(
            responseTemplates[0].first,
            "current" to xpTracker.currentRatePerHour.toLong().compact(),
            "avg" to xpTracker.overallRatePerHour.toLong().compact(),
            "time" to FishingSession.duration.toReadableString()
        )
        sendPartyMessage(response)
    }
}
