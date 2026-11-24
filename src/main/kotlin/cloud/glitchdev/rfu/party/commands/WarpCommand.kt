package cloud.glitchdev.rfu.party.commands

import cloud.glitchdev.rfu.config.categories.PartySettings
import cloud.glitchdev.rfu.constants.chat.RegexConstants.PLAYER_REGEX
import cloud.glitchdev.rfu.constants.chat.PartyCommandListMode
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.chat.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.party.AbstractPartyCommand
import cloud.glitchdev.rfu.party.PartyCommand
import cloud.glitchdev.rfu.party.PartyCommandPermission
import cloud.glitchdev.rfu.party.WarpKickManager
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlinx.coroutines.delay

@RFUFeature
@PartyCommand
object WarpCommand : AbstractPartyCommand(
    name = "warp",
    description = "Warps the party.",
    aliases = listOf("w"),
    responseTemplates = listOf(
        "No need to warp. (Togglewarp ON)" to "&cNo need to warp. (Togglewarp ON)"
    ),
    permission = listOf(PartyCommandPermission.LEADER_ONLY, PartyCommandPermission.SELF_TRIGGER)
), Feature {
    private val MUTED_MESSAGE_REGEX =
        """From ($PLAYER_REGEX): Hey! I'm currently muted and am unable to message right now\.""".toExactRegex()
    private val recentlyTriggered = mutableSetOf<String>()

    override fun onInitialize(scope: EventScope) {
        scope.onClose {
            recentlyTriggered.clear()
        }

        scope.registerGameEvent(MUTED_MESSAGE_REGEX) { _, _, match ->
            if (!PartySettings.togglePartyCommands || !PartySettings.warpOnMutedMessage || !isEnabled()) {
                return@registerGameEvent
            }
            if (!Party.inParty || !Party.isLeader) return@registerGameEvent

            val sender = match?.groupValues?.get(1)?.removeRankTag() ?: return@registerGameEvent
            val member = Party.members.keys.firstOrNull { it.equals(sender, ignoreCase = true) }
                ?: return@registerGameEvent

            val isListed = PartySettings.partyCommandPlayers.any { it.trim().equals(member, ignoreCase = true) }
            when (PartySettings.partyCommandListMode) {
                PartyCommandListMode.BLACKLIST -> if (isListed) return@registerGameEvent
                PartyCommandListMode.WHITELIST -> if (!isListed) return@registerGameEvent
            }

            if (!recentlyTriggered.add(member.lowercase())) return@registerGameEvent
            execute(member, emptyList())
            Coroutines.launch(scope) {
                delay((PartySettings.spamCooldown * 1000).toLong())
                recentlyTriggered.remove(member.lowercase())
            }
        }
    }

    override fun isEnabled() = PartySettings.toggleWarpCommand

    override fun execute(sender: String, args: List<String>) {
        WarpKickManager.executeWarpWithKicks()
    }
}
