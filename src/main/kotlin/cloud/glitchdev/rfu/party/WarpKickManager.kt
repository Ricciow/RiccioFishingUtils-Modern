package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.constants.RegexConstants.PLAYER_REGEX
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerSendCommandEvent
import cloud.glitchdev.rfu.events.managers.PartyEvents.registerOnPartyChangeEvent
import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import kotlinx.coroutines.delay

@AutoRegister
object WarpKickManager : RegisteredEvent {
    private val kickList = mutableSetOf<String>()
    private val pendingRejoin = mutableSetOf<String>()
    private var lastKicker: String? = null
    private var lastKickTime: Long = 0

    fun toggleUser(username: String): Boolean {
        return if (kickList.contains(username)) {
            kickList.remove(username)
            false
        } else {
            kickList.add(username)
            true
        }
    }

    fun isUserOnList(username: String): Boolean = kickList.contains(username)

    val kickListSize
        get() = kickList.size

    fun executeWarpWithKicks() {
        if (!Party.isLeader) {
            Chat.sendCommand("p warp")
            return
        }

        if (Party.members.size == 2 && Party.members.keys.any { isUserOnList(it) }) {
            Chat.sendPartyMessage("No need to warp. (Togglewarp ON)")
            return
        }

        val currentMembers = Party.members.keys.toList()
        val usersToKick = kickList.filter { it in currentMembers }
        
        if (usersToKick.isEmpty()) {
            Chat.sendCommand("p warp")
            return
        }

        usersToKick.forEach { user ->
            Chat.sendCommand("p kick $user")

            pendingRejoin.add(user)
            Coroutines.launch {
                delay(60000)
                if (pendingRejoin.contains(user)) {
                    kickList.remove(user)
                    pendingRejoin.remove(user)
                }
            }
        }

        Chat.sendCommand("p warp")

        usersToKick.forEach { user ->
            Chat.sendCommand("p invite $user")
        }
    }

    override fun register() {
        registerGameEvent("""You have been kicked from the party by ($PLAYER_REGEX)""".toExactRegex()) { _, _, matches ->
            val kicker = matches?.groupValues?.getOrNull(1)?.removeRankTag() ?: return@registerGameEvent
            lastKicker = kicker
            lastKickTime = System.currentTimeMillis()
        }

        registerGameEvent("""($PLAYER_REGEX) has invited you to join their party!""".toExactRegex()) { _, _, matches ->
            val inviter = matches?.groupValues?.getOrNull(1)?.removeRankTag() ?: return@registerGameEvent
            val now = System.currentTimeMillis()

            if (inviter == lastKicker && now - lastKickTime < 30000) {
                Chat.sendCommand("p join $inviter")
                lastKicker = null
            }
        }

        registerOnPartyChangeEvent { inParty, isLeader, _, members ->
            if (inParty && isLeader) {
                kickList.removeIf { it !in members.keys && it !in pendingRejoin }
                pendingRejoin.removeIf { it in members.keys }
                lastKicker = null
            } else {
                kickList.clear()
                pendingRejoin.clear()

                if (!isLeader) {
                    lastKicker = null
                }
            }
        }

        registerSendCommandEvent { command ->
            val cmd = command.lowercase().trim()
            if (cmd == "p warp" || cmd == "party warp" || cmd == "p w" || cmd == "party w") {
                if (Party.isLeader && PartySettings.toggleWarpCommand) {
                    executeWarpWithKicks()
                    return@registerSendCommandEvent false
                }
            }
            true
        }
    }
}
