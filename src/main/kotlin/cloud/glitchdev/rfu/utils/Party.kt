package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text

object Party {
    var inParty = false
    var isLeader = false
    val members: MutableSet<String> = mutableSetOf()

    private val playerRegex = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"

    fun registerEvents() {
        Chat.registerChat("""Party > .+: .+""".toExactRegex()) { _, _ ->
            inParty = true
        }

        Chat.registerChat("""You have joined ($playerRegex)'s? party!""".toExactRegex()) { _, matches ->
            inParty = true
            val username = matches[1].removeRankTag()
            members.clear()
            members.add(username)
        }

        Chat.registerChat("""You'll be partying with: ($playerRegex)""".toExactRegex()) { _, matches ->
            inParty = true
            val people = matches[1].split(", ").map { it.removeRankTag() }
            members.addAll(people)
        }

        Chat.registerChat("""Party Leader: ($playerRegex) ●""".toExactRegex()) { _, matches ->
            inParty = true
            val username = matches[1].removeRankTag()
            isLeader = username.isUser()
            members.clear()
            members.add(username)
        }

        Chat.registerChat("""Party (?:Moderators|Members): ($playerRegex)""".toExactRegex()) { _, matches ->
            inParty = true
            val people = matches[1].split(" ● ").map { it.removeRankTag() }.filter { it.isNotEmpty() && !it.isUser() }
            members.addAll(people)
        }

        Chat.registerChat("""($playerRegex) invited $playerRegex to the party! They have 60 seconds to accept\.""".toExactRegex()) { _, matches ->
            inParty = true
            val leader = matches[1].removeRankTag()
            isLeader = leader.isUser()
        }

        Chat.registerChat("""($playerRegex) joined the party\.""".toExactRegex()) { _, matches ->
            inParty = true
            val player = matches[1].removeRankTag()
            members.add(player)
        }

        Chat.registerChat("""Created a public party! Players can join with /party join ($playerRegex)""".toExactRegex()) { _, _ ->
            inParty = true
            isLeader = true
        }

        Chat.registerChat("""You're not this party's leader!""".toExactRegex()) { _, _->
            inParty = true
            isLeader = false
        }

        Chat.registerChat(
            ("You left the party\\." +
                    "|You have been kicked from the party by $playerRegex" +
                    "|The party was disbanded because the party leader disconnected\\." +
                    "|$playerRegex has disbanded the party!" +
                    "|You're not in a party right now\\.")
                .toExactRegex()
        ) { _, _ ->
            inParty = false
            isLeader = false
            members.clear()
        }

        Chat.registerChat("""The party was transfered to ($playerRegex) by ($playerRegex)""".toExactRegex()) { _, matches ->
            inParty = true
            val player1 = matches[1].removeRankTag()
            val player2 = matches[2].removeRankTag()
            isLeader = player1.isUser() && !player2.isUser()
        }

        Chat.registerChat("""The party was transfered to ($playerRegex) because ($playerRegex) left""".toExactRegex()) { _, matches ->
            val player1 = matches[1].removeRankTag()
            val player2 = matches[2].removeRankTag()
            inParty = !player2.isUser()
            isLeader = player1.isUser() && !player2.isUser()
            if (!inParty) {
                members.clear()
            }
        }

        Chat.registerChat(
            ("($playerRegex) has left the party\\." +
                    "($playerRegex) was removed from your party because they disconnected\\." +
                    "($playerRegex) has been removed from the party \\."
                    ).toExactRegex()
        ) { _, matches ->
            val player = matches[1].removeRankTag()
            members.remove(player)
        }

        Chat.registerChat("From ($playerRegex): \\[RFUPF\\] I would like to join your party!".toExactRegex()) { _, matches ->
            val player = matches[1].removeRankTag()
            promptInvite(player)
        }
    }

    fun promptInvite(username: String) {
        val text = TextUtils.rfuLiteral("$username ${TextColor.GOLD}would like to join your party ", TextStyle(TextColor.YELLOW,
            TextEffects.BOLD)) as? MutableText
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

    fun requestEntry(username : String) {
        Chat.sendServerCommand("w $username [RFUPF] I would like to join your party!")
    }
}