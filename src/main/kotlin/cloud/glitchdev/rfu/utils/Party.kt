package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.hypixelModAPI
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.events.managers.PartyEvents.registerJoinRequestEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.dsl.isUser
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

@AutoRegister
object Party : RegisteredEvent {
    var inParty = false
    var isLeader = false
    val members: MutableSet<String> = mutableSetOf()
    val listeners: MutableList<(Boolean, Boolean, MutableSet<String>) -> Unit> = mutableListOf()
    private var requestedUser: String? = null
    private val joinedCooldowns: MutableMap<String, Long> = mutableMapOf()
    private val pendingPFInvites: MutableSet<String> = mutableSetOf()

    private const val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"

    override fun register() {
        hypixelModAPI.createHandler(ClientboundPartyInfoPacket::class.java) { event ->
            inParty = event.isInParty
        }

        registerJoinEvent { wasConnected ->
            if(mc.isLocalServer) return@registerJoinEvent
            if(mc.currentServer?.ip?.endsWith("hypixel.net") == true) return@registerJoinEvent
            if(!wasConnected) hypixelModAPI.sendPacket(ServerboundPartyInfoPacket())
        }

        registerJoinRequestEvent { applicant ->
            promptInvite(applicant)
        }

        registerGameEvent("""Party > .+: .+""".toExactRegex()) { _, _, _ ->
            inParty = true
            executePartyChange()
        }

        registerGameEvent("""You have joined ($PLAYER_REGEX)'s? party!""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
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
            members.clear()
            members.add(username)
            executePartyChange()
        }

        registerGameEvent("""You'll be partying with: ($PLAYER_REGEX)""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
            val people = matchGroups[1].split(", ").map { it.removeRankTag() }
            members.addAll(people)
            executePartyChange()
        }

        registerGameEvent("""Party Leader: ($PLAYER_REGEX) ●""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
            val username = matchGroups[1].removeRankTag()
            isLeader = username.isUser()
            members.clear()
            if(!isLeader) members.add(username)
            executePartyChange()
        }

        registerGameEvent("""Party (?:Moderators|Members): (.+)""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
            val people = matchGroups[1].split(" ● ").map { it.removeRankTag() }.filter { it.isNotEmpty() && !it.isUser() }
            members.addAll(people)
            executePartyChange()
        }

        registerGameEvent("""($PLAYER_REGEX) invited $PLAYER_REGEX to the party! They have 60 seconds to accept\.""".toExactRegex()) { _, _, _ ->
            inParty = true
            executePartyChange()
        }

        registerGameEvent("""($PLAYER_REGEX) joined the party\.""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
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

            members.add(player)
            executePartyChange()
        }

        registerGameEvent("""Created a public party! Players can join with /party join ($PLAYER_REGEX)""".toExactRegex()) { _, _, _ ->
            inParty = true
            isLeader = true
            executePartyChange()
        }

        registerGameEvent("""You're not this party's leader!""".toExactRegex()) { _, _, _ ->
            inParty = true
            isLeader = false
            executePartyChange()
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
            inParty = false
            isLeader = false
            members.clear()
            executePartyChange()
        }

        registerGameEvent("""The party was transfered to ($PLAYER_REGEX) by ($PLAYER_REGEX)""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
            val player1 = matchGroups[1].removeRankTag()
            val player2 = matchGroups[2].removeRankTag()
            isLeader = player1.isUser() && !player2.isUser()
            executePartyChange()
        }

        registerGameEvent("""The party was transfered to ($PLAYER_REGEX) because ($PLAYER_REGEX) left""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val player1 = matchGroups[1].removeRankTag()
            val player2 = matchGroups[2].removeRankTag()
            inParty = !player2.isUser()
            isLeader = player1.isUser() && !player2.isUser()
            if (!inParty) {
                members.clear()
            }
            executePartyChange()
        }

        registerGameEvent(
            ("($PLAYER_REGEX) (?:has left the party\\." +
                    "|was removed from your party because they disconnected\\." +
                    "|has been removed from the party\\.)"
                    ).toExactRegex()
        ) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val player = matchGroups[1].removeRankTag()
            members.remove(player)
            executePartyChange()
        }

        registerAllowGameEvent("From ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerAllowGameEvent true
            val player = matchGroups[1].removeRankTag()
            promptInvite(player)
            return@registerAllowGameEvent false
        }

        registerAllowGameEvent("To ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, _, matches ->
            return@registerAllowGameEvent false
        }

        onPartyChange { inParty, _, members ->
            val currentParty: FishingParty? = PartyWebSocket.myParty
            if (currentParty != null) {
                if (inParty) {
                    currentParty.players.current = members.size + 1
                    PartyWebSocket.editParty(currentParty)
                } else {
                    PartyWebSocket.deleteParty(User.getUsername())
                }
            }
        }

        registerShutdownEvent {
            if (PartyWebSocket.myParty != null) {
                PartyWebSocket.deleteParty(User.getUsername())
            }
        }
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
    private var oldMembers: MutableSet<String> = mutableSetOf()

    private fun executePartyChange() {
        if (inParty != wasInParty ||
            wasLeader != isLeader ||
            oldMembers != members
        ) {
            listeners.forEach { it(inParty, isLeader, members) }
        }

        wasInParty = inParty
        wasLeader = isLeader
        oldMembers.clear()
        oldMembers.addAll(members)
    }

    fun onPartyChange(callback: (Boolean, Boolean, MutableSet<String>) -> Unit) {
        listeners.add(callback)
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