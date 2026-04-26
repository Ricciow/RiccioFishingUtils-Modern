package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.IRareDrop
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.drops.DropHistory
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@PartyCommand
object SinceCommand : AbstractPartyCommand(
    name = "since",
    description = "Shows the count and time since the last catch of a specific sea creature or rare drop.",
    aliases = listOf("s"),
    responseTemplates = listOf(
        "Since {name}: {count} catches | Last catch: {time} ago{drops}" to "&9&l{sender} &b- &6{1}&b:\n &f{2} &ecatches &7| &f{3} &eago{4}",
        "Target '{name}' not found." to "&cTarget &6'{1}' &cnot found.",
        "Usage: !since <target> [username]" to "&cUsage: &f!since &e<target> [username]",
        "Since {name}: {count} drops | Last drop: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &f{2} &edrops &7| &f{3} &eago"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun isEnabled() = PartySettings.toggleSinceCommand

    private fun findTarget(query: String): Any? {
        val sc = SeaCreatures.entries.find { 
            it.scName.contains(query, ignoreCase = true) ||
            it.scDisplayName.contains(query, ignoreCase = true)
        }
        if (sc != null) return sc
        
        val drop = RareDrops.entries.find { it.dropName.contains(query, ignoreCase = true) }
        if (drop != null) return drop
        
        val dye = Dyes.entries.find { it.dyeName.contains(query, ignoreCase = true) }
        if (dye != null) return dye
        
        return null
    }

    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            sendPartyMessage("Usage: !since <target> [username]")
            return
        }

        val myUsername = User.getUsername()
        var target: Any? = null
        var query = args.joinToString(" ")

        // Try matching whole input first
        target = findTarget(query)

        if (target == null && args.size > 1) {
            val lastArg = args.last()
            val inputWithoutUser = args.dropLast(1).joinToString(" ")
            val potentialTarget = findTarget(inputWithoutUser)

            if (potentialTarget != null) {
                if (lastArg.equals(myUsername, ignoreCase = true)) {
                    target = potentialTarget
                    query = inputWithoutUser
                } else {
                    // Intended for someone else
                    return
                }
            }
        }

        if (target == null) {
            // Only show "not found" if we are sure it was for us or no username was provided
            val isForUs = args.size > 1 && args.last().equals(myUsername, ignoreCase = true)
            if (args.size == 1 || isForUs) {
                val notFoundName = if (isForUs) args.dropLast(1).joinToString(" ") else query
                val response = formatResponse(responseTemplates[1].first, "name" to notFoundName)
                sendPartyMessage(response)
            }
            return
        }

        when (target) {
            is SeaCreatures -> {
                val record = CatchTracker.catchHistory.getOrAdd(target)
                val duration = Clock.System.now() - record.time
                
                val relatedDrops = RareDrops.entries.filter { it.relatedScs.contains(target) }
                val relatedDyes = Dyes.entries.filter { it.relatedScs.contains(target) }
                
                var dropsString = ""
                (relatedDrops + relatedDyes).forEach { drop ->
                    val entry: DropHistory.IDropEntry? = when (drop) {
                        is RareDrops -> DropManager.dropHistory.getOrAdd(drop)
                        is Dyes -> DropManager.dropHistory.getOrAdd(drop)
                        else -> null
                    }
                    if (entry != null && entry.history.isNotEmpty()) {
                        val lastDrop = entry.history.last()
                        val timeSince = (Clock.System.now() - lastDrop.date).toReadableString()
                        dropsString += " | ${drop.displayName}: ${entry.history.size} (Last: $timeSince ago)"
                    }
                }

                val response = formatResponse(
                    responseTemplates[0].first,
                    "name" to target.scDisplayName,
                    "count" to record.count,
                    "time" to duration.toReadableString(),
                    "drops" to dropsString
                )
                sendPartyMessage(response)
            }
            is IRareDrop -> {
                val entry: DropHistory.IDropEntry? = when (target) {
                    is RareDrops -> DropManager.dropHistory.getOrAdd(target)
                    is Dyes -> DropManager.dropHistory.getOrAdd(target)
                    else -> null
                }
                
                if (entry != null) {
                    if (entry.history.isEmpty()) {
                        sendPartyMessage("No history for ${target.displayName}.")
                    } else {
                        val lastDrop = entry.history.last()
                        val duration = Clock.System.now() - lastDrop.date
                        val response = formatResponse(
                            responseTemplates[3].first,
                            "name" to target.displayName,
                            "count" to entry.history.size,
                            "time" to duration.toReadableString()
                        )
                        sendPartyMessage(response)
                    }
                }
            }
        }
    }
}
