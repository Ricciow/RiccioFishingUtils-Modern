package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.utils.User
import java.util.Locale

@PartyCommand
object SchCommand : AbstractPartyCommand(
    name = "sch",
    description = "Shows your current and average Sea Creatures per hour.",
    aliases = listOf("scph"),
    responseTemplates = listOf(
        "Current SC/h: {current} | Average: {avg}" to "&9&l{sender} &b- &6SC/h&b:\n &f{1} &eCurrent &7| &f{2} &eAverage"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleSchCommand

    override fun execute(sender: String, args: List<String>) {
        if (!FishingSession.isFishing) return

        if (args.isNotEmpty()) {
            val targetUser = args[0]
            val myUsername = User.getUsername()
            if (!targetUser.equals(myUsername, ignoreCase = true)) {
                return
            }
        }

        val scTracker = FishingSession.scTracker
        val current = String.format(Locale.US, "%.1f", scTracker.currentRatePerHour)
        val avg = String.format(Locale.US, "%.1f", scTracker.overallRatePerHour)

        val response = formatResponse(
            responseTemplates[0].first,
            "current" to current,
            "avg" to avg
        )
        sendPartyMessage(response)
    }
}
