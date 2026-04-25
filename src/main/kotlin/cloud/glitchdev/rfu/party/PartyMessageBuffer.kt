package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.removeRankTag
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import net.minecraft.network.chat.Component

@AutoRegister
object PartyMessageBuffer : RegisteredEvent {
    private val PLAYER_REGEX = "(?:\\[[A-Z]+\\+*\\] )?[0-9a-zA-Z_]{3,16}"
    private val CHUNK_REGEX = """Party > ($PLAYER_REGEX): \((\d+)/(\d+)\) (.*)""".toExactRegex()
    
    private val buffers = mutableMapOf<String, MutableMap<Int, String>>()

    override fun register() {
        registerAllowGameEvent(CHUNK_REGEX, priority = 10) { _, _, matches ->
            val matchGroups = matches?.groupValues ?: return@registerAllowGameEvent true
            val sender = matchGroups[1].removeRankTag()
            val current = matchGroups[2].toInt()
            val total = matchGroups[3].toInt()
            val content = matchGroups[4]

            val userBuffer = buffers.getOrPut(sender) { mutableMapOf() }
            userBuffer[current] = content

            if (userBuffer.size == total) {
                val fullMessage = (1..total).joinToString("") { userBuffer[it] ?: "" }
                buffers.remove(sender)
                
                if (!PartyCommandManager.tryReformat(sender, fullMessage)) {
                    Chat.sendMessage(Component.literal("§dParty > §r$sender§f: $fullMessage"))
                }
            }

            false
        }
    }
}
