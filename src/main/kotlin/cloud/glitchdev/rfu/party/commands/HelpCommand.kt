package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.party.PartyCommandManager

@PartyCommand
object HelpCommand : AbstractPartyCommand(
    name = "help",
    description = "Shows all available party commands.",
    responseTemplates = listOf(
        "Available commands: {commands}" to "&bAvailable commands: &f{1}",
        "Command {name}: {description} (Aliases: {aliases}, Permission: {permission})" to "&bCommand &6{1}&b: &e{2} &7(Aliases: {3}, Permission: {4})"
    )
) {
    override fun execute(sender: String, args: List<String>) {
        if (args.isEmpty()) {
            val commandNames = PartyCommandManager.getCommands().map { it.name }.joinToString(", ")
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
                    "aliases" to if (command.aliases.isEmpty()) "None" else command.aliases.joinToString(", "),
                    "permission" to command.permission.name
                )
                sendPartyMessage(response)
            } else {
                sendPartyMessage("Command '$targetName' not found.")
            }
        }
    }
}
