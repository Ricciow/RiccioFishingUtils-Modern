package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import cloud.glitchdev.rfu.utils.network.PartyHttp
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text

object Party {
    var inParty = false
    var isLeader = false
    val members: MutableSet<String> = mutableSetOf()
    val listeners: MutableList<(Boolean, Boolean, MutableSet<String>) -> Unit> = mutableListOf()

    private const val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"

    fun registerEvents() {
        Chat.registerChat("""Party > .+: .+""".toExactRegex()) { _, _ ->
            inParty = true
            executePartyChange()
        }

        Chat.registerChat("""You have joined ($PLAYER_REGEX)'s? party!""".toExactRegex()) { _, matches ->
            inParty = true
            val username = matches[1].removeRankTag()
            members.clear()
            members.add(username)
            executePartyChange()
        }

        Chat.registerChat("""You'll be partying with: ($PLAYER_REGEX)""".toExactRegex()) { _, matches ->
            inParty = true
            val people = matches[1].split(", ").map { it.removeRankTag() }
            members.addAll(people)
            executePartyChange()
        }

        Chat.registerChat("""Party Leader: ($PLAYER_REGEX) ●""".toExactRegex()) { _, matches ->
            inParty = true
            val username = matches[1].removeRankTag()
            isLeader = username.isUser()
            members.clear()
            members.add(username)
            executePartyChange()
        }

        Chat.registerChat("""Party (?:Moderators|Members): (.+)""".toExactRegex()) { _, matches ->
            inParty = true
            val people = matches[1].split(" ● ").map { it.removeRankTag() }.filter { it.isNotEmpty() && !it.isUser() }
            members.addAll(people)
            executePartyChange()
        }

        Chat.registerChat("""($PLAYER_REGEX) invited $PLAYER_REGEX to the party! They have 60 seconds to accept\.""".toExactRegex()) { _, _ ->
            inParty = true
            executePartyChange()
        }

        Chat.registerChat("""($PLAYER_REGEX) joined the party\.""".toExactRegex()) { _, matches ->
            inParty = true
            val player = matches[1].removeRankTag()
            members.add(player)
            executePartyChange()
        }

        Chat.registerChat("""Created a public party! Players can join with /party join ($PLAYER_REGEX)""".toExactRegex()) { _, _ ->
            inParty = true
            isLeader = true
            executePartyChange()
        }

        Chat.registerChat("""You're not this party's leader!""".toExactRegex()) { _, _ ->
            inParty = true
            isLeader = false
            executePartyChange()
        }

        Chat.registerChat(
            ("You left the party\\." +
                    "|You have been kicked from the party by $PLAYER_REGEX" +
                    "|The party was disbanded because the party leader disconnected\\." +
                    "|The party was disbanded because all invites expired and the party was empty\\." +
                    "|$PLAYER_REGEX has disbanded the party!" +
                    "|You're not in a party right now\\.")
                .toExactRegex()
        ) { _, _ ->
            inParty = false
            isLeader = false
            members.clear()
            executePartyChange()
        }

        Chat.registerChat("""The party was transfered to ($PLAYER_REGEX) by ($PLAYER_REGEX)""".toExactRegex()) { _, matches ->
            inParty = true
            val player1 = matches[1].removeRankTag()
            val player2 = matches[2].removeRankTag()
            isLeader = player1.isUser() && !player2.isUser()
            executePartyChange()
        }

        Chat.registerChat("""The party was transfered to ($PLAYER_REGEX) because ($PLAYER_REGEX) left""".toExactRegex()) { _, matches ->
            val player1 = matches[1].removeRankTag()
            val player2 = matches[2].removeRankTag()
            inParty = !player2.isUser()
            isLeader = player1.isUser() && !player2.isUser()
            if (!inParty) {
                members.clear()
            }
            executePartyChange()
        }

        Chat.registerChat(
            ("($PLAYER_REGEX) (?:has left the party\\." +
                    "|was removed from your party because they disconnected\\." +
                    "|has been removed from the party\\.)"
                    ).toExactRegex()
        ) { _, matches ->
            val player = matches[1].removeRankTag()
            members.remove(player)
            executePartyChange()
        }

        Chat.registerChat("From ($PLAYER_REGEX): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, matches ->
            val player = matches[1].removeRankTag()
            promptInvite(player)
        }

        onPartyChange { inParty, _, members ->
            val currentParty : FishingParty? = PartyHttp.currentParty
            if(currentParty != null) {
                PartyHttp.deleteParty { success ->
                 if(success && inParty && members.size + 1 <= currentParty.players.max) {
                        currentParty.players.current = members.size + 1
                        PartyHttp.createParty(currentParty) {}
                    }
                }
            }
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            if(PartyHttp.currentParty != null) {
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
        ) as? MutableText
        text?.append(
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