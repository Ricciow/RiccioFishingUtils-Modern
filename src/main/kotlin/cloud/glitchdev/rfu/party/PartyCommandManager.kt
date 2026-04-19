package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.party.commands.HelpCommand
import cloud.glitchdev.rfu.party.commands.IPartyCommand
import cloud.glitchdev.rfu.party.commands.PartyCommandPermission
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.delay
import net.minecraft.network.chat.Component

@AutoRegister
object PartyCommandManager : RegisteredEvent {
    private val commands = mutableMapOf<String, IPartyCommand>()
    private val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"
    
    private val recentlyExecuted = mutableSetOf<String>()

    fun register(command: IPartyCommand) {
        commands[command.name.lowercase()] = command
        command.aliases.forEach { commands[it.lowercase()] = command }
    }

    override fun register() {
        registerAllowGameEvent { text, _, _ ->
            if (!PartySettings.togglePartyCommands) return@registerAllowGameEvent true
            
            val message = text.toUnformattedString()
            val prefix = PartySettings.partyCommandPrefix
            val regex = """Party > ($PLAYER_REGEX): ${Regex.escape(prefix)}(\w+)(.*)""".toRegex()
            
            val match = regex.find(message) ?: return@registerAllowGameEvent true
            val matchGroups = match.groupValues
            val sender = matchGroups[1].removeRankTag()
            val commandName = matchGroups[2].lowercase()
            val argsRaw = matchGroups[3].trim()
            val args = if (argsRaw.isEmpty()) emptyList() else argsRaw.split(" ")

            val command = commands[commandName] ?: return@registerAllowGameEvent true
            
            if (shouldExecute(command, sender)) {
                val executionKey = "$sender:$commandName:$argsRaw"
                if (recentlyExecuted.contains(executionKey)) return@registerAllowGameEvent true

                command.execute(sender, args)

                recentlyExecuted.add(executionKey)
                Coroutines.launch {
                    delay((PartySettings.spamCooldown * 1000).toLong())
                    recentlyExecuted.remove(executionKey)
                }
            }

            true
        }
    }

    fun tryReformat(sender: String, message: String): Boolean {
        val trimmedMessage = message.trim()
        for (command in getCommands()) {
            command.responseTemplates.forEachIndexed { index, templatePair ->
                val regex = templateToRegex(templatePair.first, exact = true)
                val match = regex.matchEntire(trimmedMessage)
                if (match != null) {
                    val richResponse = command.getRichResponse(sender, trimmedMessage, index, match)
                    Chat.sendMessage(richResponse)
                    return true
                }
            }
        }
        return false
    }

    private fun templateToRegex(template: String, exact: Boolean = false): Regex {
        var pattern = template.escapeForRegex()
        pattern = pattern.replace(Regex("""\\\{.*?\\\}"""), "(.*)")
        pattern = pattern.replace(" ", "\\s+")
        return (if (exact) "^$pattern$" else pattern).toRegex()
    }

    private fun shouldExecute(command: IPartyCommand, sender: String): Boolean {
        if (command is HelpCommand && !PartySettings.toggleHelpCommand) return false
        
        val myName = mc.player?.gameProfile?.name ?: return false
        val isMe = sender == myName

        return when (command.permission) {
            PartyCommandPermission.SELF_TRIGGER -> isMe
            PartyCommandPermission.LEADER_ONLY -> Party.isLeader
            PartyCommandPermission.MEMBER_ONLY -> !Party.isLeader && Party.inParty
            PartyCommandPermission.ANY -> true
        }
    }
    
    fun getCommands(): Collection<IPartyCommand> {
        val allCommands = commands.values.distinct()
        if (!PartySettings.toggleHelpCommand) {
            return allCommands.filter { it !is HelpCommand }
        }
        return allCommands
    }
}
