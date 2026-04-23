package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@PartyCommand
object SinceCommand : AbstractPartyCommand(
    name = "since",
    description = "Shows the count and time since the last catch of a specific sea creature.",
    aliases = listOf("s"),
    responseTemplates = listOf(
        "Since {name}: {count} catches | Last catch: {time} ago" to "&bSince &6{1}&b: &e{2} catches &7| &bLast catch: &e{3} ago",
        "Sea creature '{name}' not found." to "&cSea creature '&6{1}&c' not found.",
        "Usage: !since <sea creature>" to "&cUsage: &f!since &e<sea creature>"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleSinceCommand

    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            sendPartyMessage("Usage: !since <sea creature>")
            return
        }

        val input = args.joinToString(" ").lowercase()
        val sc = SeaCreatures.entries.find { 
            it.scName.lowercase().contains(input)
        }

        if (sc != null) {
            val record = CatchTracker.catchHistory.getOrAdd(sc)
            val duration = Clock.System.now() - record.time
            val response = formatResponse(
                responseTemplates[0].first,
                "name" to sc.getNameWithoutArticle(),
                "count" to record.count,
                "time" to duration.toReadableString()
            )
            sendPartyMessage(response)
        } else {
            val response = formatResponse(responseTemplates[1].first, "name" to input)
            sendPartyMessage(response)
        }
    }
}
