package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@AutoRegister
object PartyResponseFormatter : RegisteredEvent {
    private val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"
    private val PARTY_CHAT_PREFIX_REGEX = """Party > ($PLAYER_REGEX): (.*)""".toExactRegex()

    override fun register() {
        registerAllowGameEvent(PARTY_CHAT_PREFIX_REGEX, priority = 25) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerAllowGameEvent true
            val sender = matchGroups[1].removeRankTag()
            val message = matchGroups[2]

            if (PartyCommandManager.tryReformat(sender, message)) {
                return@registerAllowGameEvent false
            }

            true
        }
    }
}
