package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission

@PartyCommand
object StreakCommand : AbstractPartyCommand(
    name = "streak",
    description = "Shows the current daily streak.",
    aliases = listOf("st"),
    responseTemplates = listOf(
        "Current Daily Streak: {count} | Highest: {highest}" to "&6&lDaily Streak - &9&l{sender}&6&l: &f&l{1} Days",
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun execute(sender: String, args: List<String>) {
        val response = formatResponse(responseTemplates[0].first, "count" to DailyStreakManager.data.currentStreak, "highest" to DailyStreakManager.data.highestStreak)
        sendPartyMessage(response)
    }
}