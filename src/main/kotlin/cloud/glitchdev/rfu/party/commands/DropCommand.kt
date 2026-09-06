package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.constants.fishing.IRareDrop
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.drops.DropHistory
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import java.util.Locale
import kotlin.time.Clock

@PartyCommand
object DropCommand : AbstractPartyCommand(
    name = "drop",
    description = "Shows detailed drop stats about a specific rare drop or dye.",
    aliases = listOf("drops", "d"),
    responseTemplates = listOf(
        "Drop {name}: Total: {total} ({breakdown}) | Count: {streak} sc | Avg: {avg} sc | Chance: {chance} | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7({3}) &7| &eCount: &f{4} &esc &7| &eAvg: &f{5} &esc &7| &eChance: &f{6} &7| &eLast: &f{7} &eago",
        "Drop {name}: Total: {total} | Count: {streak} sc | Avg: {avg} sc | Chance: {chance} | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7| &eCount: &f{3} &esc &7| &eAvg: &f{4} &esc &7| &eChance: &f{5} &7| &eLast: &f{6} &eago",
        "Drop '{name}' not found." to "&cDrop &6'{1}' &cnot found.",
        "Usage: !drop <item> [username]" to "&cUsage: &f!drop &e<item> [username]",
        "Drop {name}: Total: {total} ({breakdown}) | Count: {streak} sc | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7({3}) &7| &eCount: &f{4} &esc &7| &eLast: &f{5} &eago",
        "Drop {name}: Total: {total} | Count: {streak} sc | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7| &eCount: &f{3} &esc &7| &eLast: &f{4} &eago",
        "Drop {name}: Total: 0 | Count: {streak} sc | Last: Never" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f0 &7| &eCount: &f{2} &esc &7| &eLast: &cNever",
        "Drop {name}: Total: {total} | Last: {time} ago" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f{2} &7| &eLast: &f{3} &eago",
        "Drop {name}: Total: 0 | Last: Never" to "&9&l{sender} &b- &6{1}&b:\n &eTotal: &f0 &7| &eLast: &cNever"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {

    override fun isEnabled() = PartySettings.toggleDropCommand

    private fun findTarget(query: String): IRareDrop? {
        val drop = RareDrops.entries.find { it.dropName.contains(query, ignoreCase = true) }
        if (drop != null) return drop

        val dye = Dyes.entries.find { it.dyeName.contains(query, ignoreCase = true) }
        if (dye != null) return dye

        return null
    }

    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            sendPartyMessage(responseTemplates[3].first)
            return
        }

        val myUsername = User.getUsername()
        var target: IRareDrop? = null
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
                val response = formatResponse(responseTemplates[2].first, "name" to notFoundName)
                sendPartyMessage(response)
            }
            return
        }

        val entry = when (target) {
            is RareDrops -> DropManager.dropHistory.getOrAdd(target)
            is Dyes -> DropManager.dropHistory.getOrAdd(target)
            else -> null
        } ?: return

        val lastDrop = entry.history.lastOrNull()
        val hasRelatedScs = target.relatedScs.isNotEmpty()

        if (lastDrop == null) {
            if (hasRelatedScs) {
                val currentTotal = target.relatedScs.sumOf { sc -> CatchTracker.catchHistory.getOrAdd(sc).total }
                val response = formatResponse(
                    responseTemplates[6].first,
                    "name" to target.displayName,
                    "streak" to currentTotal
                )
                sendPartyMessage(response)
            } else {
                val response = formatResponse(
                    responseTemplates[8].first,
                    "name" to target.displayName
                )
                sendPartyMessage(response)
            }
            return
        }

        val duration = Clock.System.now() - lastDrop.date
        val timeString = duration.toReadableString()
        val totalDrops = entry.history.size

        val breakdown = if (target.relatedScs.size > 1) {
            val counts = target.relatedScs.map { sc ->
                sc.scName to entry.history.count { it.mobName.equals(sc.scName, ignoreCase = true) }
            }
            val relevant = if (target.relatedScs.size <= 3) counts else counts.filter { it.second > 0 }
            if (relevant.isNotEmpty()) {
                relevant.joinToString(", ") { "${it.first}: ${it.second}" }
            } else {
                null
            }
        } else {
            null
        }

        if (hasRelatedScs) {
            val currentTotal = target.relatedScs.sumOf { sc -> CatchTracker.catchHistory.getOrAdd(sc).total }
            val scSince = (currentTotal - lastDrop.totalCount).coerceAtLeast(0)
            val sinceCounts = entry.history.mapNotNull { it.sinceCount }
            val avgVal = if (sinceCounts.isNotEmpty()) sinceCounts.average() else null
            val avg = avgVal?.let { String.format(Locale.US, "%.1f", it) }
            val chance = avgVal?.let {
                val chanceVal = if (it > 0) (1.0 / it) * 100 else 0.0
                String.format(Locale.US, "%.2f%%", chanceVal)
            }

            if (avg != null && chance != null) {
                val response = if (breakdown != null) {
                    formatResponse(
                        responseTemplates[0].first,
                        "name" to target.displayName,
                        "total" to totalDrops,
                        "breakdown" to breakdown,
                        "streak" to scSince,
                        "avg" to avg,
                        "chance" to chance,
                        "time" to timeString
                    )
                } else {
                    formatResponse(
                        responseTemplates[1].first,
                        "name" to target.displayName,
                        "total" to totalDrops,
                        "streak" to scSince,
                        "avg" to avg,
                        "chance" to chance,
                        "time" to timeString
                    )
                }
                sendPartyMessage(response)
            } else {
                val response = if (breakdown != null) {
                    formatResponse(
                        responseTemplates[4].first,
                        "name" to target.displayName,
                        "total" to totalDrops,
                        "breakdown" to breakdown,
                        "streak" to scSince,
                        "time" to timeString
                    )
                } else {
                    formatResponse(
                        responseTemplates[5].first,
                        "name" to target.displayName,
                        "total" to totalDrops,
                        "streak" to scSince,
                        "time" to timeString
                    )
                }
                sendPartyMessage(response)
            }
        } else {
            val response = formatResponse(
                responseTemplates[7].first,
                "name" to target.displayName,
                "total" to totalDrops,
                "time" to timeString
            )
            sendPartyMessage(response)
        }
    }
}
