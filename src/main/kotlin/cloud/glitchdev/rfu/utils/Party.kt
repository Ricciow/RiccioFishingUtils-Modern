package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.utils.network.PartyHttp
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text

@AutoRegister
object Party : RegisteredEvent {
    var inParty = false
    var isLeader = false
    val members: MutableSet<String> = mutableSetOf()
    val listeners: MutableList<(Boolean, Boolean, MutableSet<String>) -> Unit> = mutableListOf()

    private const val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"

    override fun register() {
        registerGameEvent("""Party > .+: .+""".toExactRegex()) { _, _, _ ->
            inParty = true
            executePartyChange()
        }

        registerGameEvent("""You have joined ($PLAYER_REGEX)'s? party!""".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            inParty = true
            val username = matchGroups[1].removeRankTag()
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

        registerGameEvent("From ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerGameEvent
            val player = matchGroups[1].removeRankTag()
            promptInvite(player)
        }

        onPartyChange { inParty, _, members ->
            val currentParty: FishingParty? = PartyHttp.currentParty
            if (currentParty != null) {
                if (inParty) {
                    if (members.size + 1 <= currentParty.players.max) {
                        currentParty.players.current = members.size + 1
                        PartyHttp.updateParty(currentParty) {}
                    }
                } else {
                    PartyHttp.deleteParty {}
                }
            }
        }

        registerShutdownEvent {
            if (PartyHttp.currentParty != null) {
                PartyHttp.deleteParty {}
            }
        }
    }

    fun promptInvite(username: String) {
        val text = TextUtils.rfuLiteral(
            "$username ${TextColor.GOLD}would like to join your party ", TextStyle(
                TextColor.YELLOW,
                TextEffects.BOLD
            )
        )
        text.append(
            Text.literal("§a[Accept]")
                .setStyle(
                    Style.EMPTY
                        .withClickEvent(ClickEvent.RunCommand("party $username"))
                        .withHoverEvent(HoverEvent.ShowText(Text.literal("/party $username")))
                )
        )
        Chat.sendMessage(text as Text)
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
        Chat.sendServerCommand("w $username [RFUPF] I would like to join your party!")
    }
}