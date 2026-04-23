package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandManager
import cloud.glitchdev.rfu.party.PartyCommandPermission

@PartyCommand
object HelpCommand : AbstractPartyCommand(
    name = "help",
    description = "Shows all available party commands.",
    aliases = listOf("h"),
    responseTemplates = listOf(
        "Available commands: {commands}" to "&bAvailable commands: &f{1}",
        "Command {name}: {description} (Aliases: {aliases})" to "&bCommand &6{1}&b: &e{2} &7(Aliases: {3})"
    ),
    permission = listOf(PartyCommandPermission.SELF_TRIGGER)
) {
    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            val commandNames = PartyCommandManager.getCommands().joinToString(", ") { "!${it.name}" }
            val response = formatResponse(responseTemplates[0].first, "commands" to commandNames)
            sendPartyMessage(response)
        } else {
            val targetName = args[0].lowercase()
            val command = PartyCommandManager.getCommands().find { 
                it.name == targetName || it.aliases.contains(targetName) 
            }

            if (command != null) {
                val response = formatResponse(
                    responseTemplates[1].first,
                    "name" to command.name,
                    "description" to command.description,
                    "aliases" to if (command.aliases.isEmpty()) "None" else command.aliases.joinToString(", ")
                )
                sendPartyMessage(response)
            } else {
                sendPartyMessage("Command '$targetName' not found.")
            }
        }
    }
}
