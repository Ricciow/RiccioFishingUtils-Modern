package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import java.util.Locale
import kotlin.time.Clock

@PartyCommand
object ScCommand : AbstractPartyCommand(
    name = "sc",
    description = "Shows detailed catch stats about a specific sea creature.",
    aliases = listOf("seacreature"),
    responseTemplates = listOf(
        "SC {name}: Total: {total} | Count: {streak} | Avg: {avg} | Chance: {chance} | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7| &eCount: &f{3} &7| &eAvg: &f{4} &7| &eChance: &f{5} &7| &eLast: &f{6} &eago",
        "Sea creature '{name}' not found." to "&cSea creature &6'{1}' &cnot found.",
        "Usage: !sc <creature> [username]" to "&cUsage: &f!sc &e<creature> [username]",
        "SC {name}: Total: {total} | Count: {streak} | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7| &eCount: &f{3} &7| &eLast: &f{4} &eago",
        "SC {name}: Total: 0 | Count: {streak} | Last: Never" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f0 &7| &eCount: &f{2} &7| &eLast: &cNever"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleScCommand

    private fun findTarget(query: String): SeaCreatures? {
        return SeaCreatures.entries.find { 
            it.scName.contains(query, ignoreCase = true) ||
            it.scDisplayName.contains(query, ignoreCase = true)
        }
    }

    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            sendPartyMessage(responseTemplates[2].first)
            return
        }

        val myUsername = User.getUsername()
        var target: SeaCreatures? = null
        var query = args.joinToString(" ")

        target = findTarget(query)

        if (target == null && args.size > 1) {
            val lastArg = args.last()
            val inputWithoutUser = args.dropLast(1).joinToString(" ")
            val potentialTarget = findTarget(inputWithoutUser)
            if (potentialTarget != null) {
                if (myUsername.contains(lastArg, ignoreCase = true)) {
                    target = potentialTarget
                    query = inputWithoutUser
                } else {
                    return
                }
            }
        }

        if (target == null) {
            val isForPlayer = args.size > 1 && myUsername.contains(args.last(), ignoreCase = true)
            if (args.size == 1 || isForPlayer) {
                val notFoundName = if (isForPlayer) args.dropLast(1).joinToString(" ") else query
                val response = formatResponse(responseTemplates[1].first, "name" to notFoundName)
                sendPartyMessage(response)
            }
            return
        }

        val record = CatchTracker.catchHistory.getOrAdd(target)

        if (record.total == 0) {
            val response = formatResponse(
                responseTemplates[4].first,
                "name" to target.scDisplayName,
                "streak" to record.count
            )
            sendPartyMessage(response)
            return
        }

        val duration = Clock.System.now() - record.time
        val timeString = duration.toReadableString()

        if (record.history.isNotEmpty()) {
            val avgVal = record.history.average()
            val avg = String.format(Locale.US, "%.1f", avgVal)
            val chanceVal = if (avgVal > 0) (1.0 / avgVal) * 100 else 0.0
            val chance = String.format(Locale.US, "%.2f%%", chanceVal)

            val response = formatResponse(
                responseTemplates[0].first,
                "name" to target.scDisplayName,
                "total" to record.total,
                "streak" to record.count,
                "avg" to avg,
                "chance" to chance,
                "time" to timeString
            )
            sendPartyMessage(response)
        } else {
            val response = formatResponse(
                responseTemplates[3].first,
                "name" to target.scDisplayName,
                "total" to record.total,
                "streak" to record.count,
                "time" to timeString
            )
            sendPartyMessage(response)
        }
    }
}
