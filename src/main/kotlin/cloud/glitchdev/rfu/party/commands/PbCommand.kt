package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Duration.Companion.milliseconds

@PartyCommand
object PbCommand : AbstractPartyCommand(
    name = "pb",
    description = "Shows your personal best kill time for a specific sea creature.",
    responseTemplates = listOf(
        "PB {name}: {time}" to "&9&l{sender} &b- &6{1}&b:\n &ePersonal Best: &f{2}",
        "No personal best kill time saved for {name}." to "&9&l{sender} &b- &6{1}&b:\n &cNo personal best kill time saved.",
        "Sea creature '{name}' not found." to "&cSea creature &6'{1}' &cnot found.",
        "Usage: !pb <creature> [username]" to "&cUsage: &f!pb &e<creature> [username]"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.togglePbCommand

    private fun findTarget(query: String): SeaCreatures? {
        return SeaCreatures.entries.find {
            it.scName.equals(query, ignoreCase = true) ||
                it.scDisplayName.equals(query, ignoreCase = true)
        } ?: SeaCreatures.entries.find {
            it.scName.contains(query, ignoreCase = true) ||
                it.scDisplayName.contains(query, ignoreCase = true)
        }
    }

    override fun execute(sender: String, args: List<String>) {
        val arguments = args.filter { it.isNotBlank() }
        if (arguments.isEmpty()) {
            sendPartyMessage(responseTemplates[3].first)
            return
        }

        val myUsername = User.getUsername()
        val query = arguments.joinToString(" ")
        var target = findTarget(query)

        if (target == null && arguments.size > 1) {
            val potentialTarget = findTarget(arguments.dropLast(1).joinToString(" "))
            if (potentialTarget != null) {
                if (!myUsername.contains(arguments.last(), ignoreCase = true)) return
                target = potentialTarget
            }
        }

        if (target == null) {
            val isForPlayer = arguments.size > 1 && myUsername.contains(arguments.last(), ignoreCase = true)
            if (arguments.size == 1 || isForPlayer) {
                val notFoundName = if (isForPlayer) arguments.dropLast(1).joinToString(" ") else query
                sendPartyMessage(formatResponse(responseTemplates[2].first, "name" to notFoundName))
            }
            return
        }

        val best = CatchTracker.catchHistory.catches.find { it.name == target.scName }?.bestKillTimeMs
        if (best == null) {
            sendPartyMessage(formatResponse(responseTemplates[1].first, "name" to target.scDisplayName))
            return
        }

        sendPartyMessage(formatResponse(
            responseTemplates[0].first,
            "name" to target.scDisplayName,
            "time" to best.milliseconds.toReadableString(true)
        ))
    }
}
