package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.RegexConstants.PLAYER_REGEX
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.hypixelModAPI
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerJoinRequestEvent
import cloud.glitchdev.rfu.events.managers.PartyEvents
import cloud.glitchdev.rfu.events.managers.PartyEvents.registerOnPartyChangeEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.dsl.isIgnored
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@AutoRegister
object Party : RegisteredEvent {
    var inParty = false
    var isLeader = false
    var isAllInvite = false
    val members: MutableMap<String, ClientboundPartyInfoPacket.PartyRole> = mutableMapOf()
    var requestedUser: String? = null
    private val joinedCooldowns: MutableMap<String, Long> = mutableMapOf()
    private val pendingPFInvites: MutableSet<String> = mutableSetOf()
    private var wasInServer = false
    private val uuidToNameCache = mutableMapOf<UUID, String>()
    private val partyInfoCallbacks = mutableListOf<() -> Unit>()

    override fun register() {
        hypixelModAPI.createHandler(ClientboundPartyInfoPacket::class.java) { event ->
            Coroutines.launch {
                inParty = event.isInParty
                isLeader = event.leader.getOrNull()?.equals(mc.player?.uuid) ?: false

                members.clear()
                if (inParty) {
                    event.memberMap.forEach { uuid, member ->
                        getUsernameFromUUID(uuid)?.let { members[it] = member.role }
                    }
                }
                executePartyChange()

                val callbacks = partyInfoCallbacks.toList()
                partyInfoCallbacks.clear()
                callbacks.forEach { it() }
            }
        }

        registerLocationEvent {
            if(!wasInServer) {
                wasInServer = true
                requestPartyInfo()
            }
        }

        registerDisconnectEvent {
            wasInServer = false
            uuidToNameCache.clear()
        }

        registerJoinRequestEvent { applicant ->
            promptInvite(applicant)
        }

        registerGameEvent("""You have joined ($PLAYER_REGEX)'s? party!""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val username = matchGroups[1].removeRankTag()
            if (username == requestedUser) {
                val lastJoin = joinedCooldowns[username] ?: 0L
                val now = System.currentTimeMillis()
                if (now - lastJoin > 15 * 60 * 1000) {
                    PartyFinderEvents.runPartyJoinedTasks()
                    joinedCooldowns[username] = now
                }
            }
            requestedUser = null
            requestPartyInfo()
        }

        registerGameEvent("""($PLAYER_REGEX) joined the party\.""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val player = matchGroups[1].removeRankTag()

            if (pendingPFInvites.contains(player)) {
                val lastJoin = joinedCooldowns[player] ?: 0L
                val now = System.currentTimeMillis()
                if (now - lastJoin > 15 * 60 * 1000) {
                    PartyFinderEvents.runPartyJoinedTasks()
                    joinedCooldowns[player] = now
                }
                pendingPFInvites.remove(player)
            }
            requestPartyInfo()
        }

        registerGameEvent("""Party > ($PLAYER_REGEX): .*""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val username = matchGroups[1].removeRankTag()
            if (!members.containsKey(username)) {
                requestPartyInfo()
            }
        }

        registerGameEvent(
            ("You left the party\\." +
                    "|You have been kicked from the party by $PLAYER_REGEX" +
                    "|The party was disbanded because the party leader disconnected\\." +
                    "|The party was disbanded because all invites expired and the party was empty\\." +
                    "|$PLAYER_REGEX has disbanded the party!" +
                    "|You're not in a party right now\\.")
                .toExactRegex()
        ) { _, _, _ ->
            requestPartyInfo()
        }

        registerGameEvent("""Created a public party! Players can join with /party join .+""".toExactRegex()) { _, _, _ ->
            requestPartyInfo()
        }

        registerGameEvent("""$PLAYER_REGEX invited $PLAYER_REGEX to the party! They have 60 seconds to accept.""".toExactRegex()) { _, _, _ ->
            if(!inParty) {
                requestPartyInfo()
            }
        }

        registerGameEvent("""The party was transferred to ($PLAYER_REGEX) by ($PLAYER_REGEX)""".toExactRegex()) { _, _, _ ->
            requestPartyInfo()
        }

        registerGameEvent("""The party was transferred to ($PLAYER_REGEX) because ($PLAYER_REGEX) left""".toExactRegex()) { _, _, _ ->
            requestPartyInfo()
        }

        registerGameEvent(
            ("($PLAYER_REGEX) (?:has left the party\\." +
                    "|was removed from your party because they disconnected\\." +
                    "|has been removed from the party\\.)"
                    ).toExactRegex()
        ) { _, _, _ ->
            requestPartyInfo()
        }

        registerGameEvent("$PLAYER_REGEX enabled All Invite".toExactRegex()) { _, _, _ ->
            isAllInvite = true
            executePartyChange()
        }

        registerGameEvent("$PLAYER_REGEX disabled All Invite".toExactRegex()) { _, _, _ ->
            isAllInvite = false
            executePartyChange()
        }

        registerAllowGameEvent("From ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerAllowGameEvent true
            val player = matchGroups[1].removeRankTag()
            if (player.isIgnored()) return@registerAllowGameEvent false
            promptInvite(player)
            return@registerAllowGameEvent false
        }

        registerAllowGameEvent("To ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, _, _ ->
            return@registerAllowGameEvent false
        }

        registerOnPartyChangeEvent { inParty, isLeader, _, members ->
            val currentParty: FishingParty? = PartyWebSocket.myParty
            if (currentParty != null) {
                if (isLeader) {
                    currentParty.players.current = members.size
                    PartyWebSocket.editParty(currentParty)
                } else {
                    PartyWebSocket.deleteParty(User.getUsername())
                }
            }
        }

        PartyFinderEvents.MyPartyChanged.register { party ->
            if (party != null && inParty && !isLeader) {
                PartyWebSocket.deleteParty(User.getUsername())
            }
        }

        registerShutdownEvent {
            if (PartyWebSocket.myParty != null) {
                PartyWebSocket.deleteParty(User.getUsername())
            }
        }
    }

    fun requestPartyInfo(callback: (() -> Unit)? = null) {
        callback?.let { partyInfoCallbacks.add(it) }
        Coroutines.launch { 
            hypixelModAPI.sendPacket(ServerboundPartyInfoPacket())
        }
    }

    private fun getUsernameFromUUID(uuid: UUID): String? {
        uuidToNameCache[uuid]?.let { return it }
        val profile = mc.services().sessionService.fetchProfile(uuid, false)
        val name = profile?.profile?.name
        if (name != null) {
            uuidToNameCache[uuid] = name
        }
        return name
    }

    fun promptInvite(username: String) {
        val text = Component.literal("${TextColor.CYAN}${TextEffects.STRIKE}-----------------------------------------------------\n")
        text.append(
            TextUtils.rfupfLiteral(
            "$username ${TextColor.GOLD}would like to join your party!", TextStyle(
                TextColor.YELLOW,
                TextEffects.BOLD
                )
            )
        )
        text.append(
            Component.literal("\n${TextColor.LIGHT_GREEN}${TextEffects.BOLD}          [Accept]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(ClickEvent.RunCommand("rfusendinvite $username"))
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("/party $username")))
                )
        )
        text.append(
            Component.literal("${TextColor.LIGHT_RED}${TextEffects.BOLD} [Deny]")
                .setStyle(
                    Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("This does nothing :dog:")))
                )
        )
        text.append(
            Component.literal("\n${TextColor.CYAN}${TextEffects.STRIKE}-----------------------------------------------------")
        )
        Chat.sendMessage(text as Component)
    }

    private var wasInParty: Boolean = false
    private var wasLeader: Boolean = false
    private var wasAllInvite: Boolean = false
    private var oldMembers: MutableMap<String, ClientboundPartyInfoPacket.PartyRole> = mutableMapOf()

    private fun executePartyChange() {
        if (inParty != wasInParty ||
            wasLeader != isLeader ||
            wasAllInvite != isAllInvite ||
            oldMembers != members
        ) {
            PartyEvents.OnPartyChange.runTasks(inParty, isLeader, isAllInvite, members)
        }

        wasInParty = inParty
        wasLeader = isLeader
        oldMembers.clear()
        oldMembers.putAll(members)
    }

    fun requestEntry(username: String) {
        requestedUser = username
        Chat.sendCommand("w $username [RFUPF] I would like to join your party!")
    }

    @Command
    object acceptPlayerPartyCommand : AbstractCommand("rfusendinvite") {
        override val description: String = "Sends an invite + some other party finder back-end stuff"

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder.then(
                arg("username", StringArgumentType.string())
                    .executes { context ->
                        val username = StringArgumentType.getString(context, "username")

                        Chat.sendCommand("party $username")
                        pendingPFInvites.add(username)

                        1
                    }
            )
        }
    }
}