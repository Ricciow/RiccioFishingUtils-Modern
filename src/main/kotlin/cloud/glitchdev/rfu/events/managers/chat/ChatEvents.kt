package cloud.glitchdev.rfu.events.managers.chat

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.registration.Defaults
import cloud.glitchdev.rfu.events.registration.ScopedRegistration
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.universal.utils.toFormattedString
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.network.chat.Component

@AutoRegister
object ChatEvents : RegisteredEvent {
    private var lastHandledChatMessage: Component? = null
    private var lastHandledGameMessage: Component? = null
    private var lastHandledGameOverlay = false

    override fun register(scope: EventScope) {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            if (Chat.isSendingModMessage) return@register true
            lastHandledChatMessage = message
            val result = ChatEventManager.runTasks(message)
            if(!result) RFULogger.dev("Chat message was hid: ${message.toFormattedString()}")
            result
        }

        ClientReceiveMessageEvents.CHAT.register { _, _, _, _, _ ->
            if (!Chat.isSendingModMessage) {
                lastHandledChatMessage = null
            }
        }

        ClientReceiveMessageEvents.CHAT_CANCELED.register { message, _, _, _, _ ->
            if (Chat.isSendingModMessage) return@register
            if (lastHandledChatMessage === message) {
                lastHandledChatMessage = null
                return@register
            }
            ChatEventManager.runTasks(message)
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (Chat.isSendingModMessage) return@register true
            lastHandledGameMessage = message
            lastHandledGameOverlay = overlay
            val result = GameEventManager.runTasks(message, overlay)
            if(!result) RFULogger.dev("Game message was hid: ${message.toFormattedString()}")
            result
        }

        ClientReceiveMessageEvents.GAME.register { _, _ ->
            if (!Chat.isSendingModMessage) {
                lastHandledGameMessage = null
            }
        }

        ClientReceiveMessageEvents.GAME_CANCELED.register { message, overlay ->
            if (Chat.isSendingModMessage) return@register
            if (lastHandledGameMessage === message && lastHandledGameOverlay == overlay) {
                lastHandledGameMessage = null
                return@register
            }
            GameEventManager.runTasks(message, overlay)
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            val result = SendChatEventManager.runTasks(message)
            if(!result) RFULogger.dev("Sent message was cancelled: $message")
            result
        }

        ClientSendMessageEvents.MODIFY_CHAT.register { message ->
            ModifyChatEventManager.runTasks(message)
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { command ->
            if (Chat.isSendingModCommand) return@register true
            val result = SendCommandEventManager.runTasks(command)
            if(!result) RFULogger.dev("Sent commnad was cancelled: $command")
            result
        }

        ClientSendMessageEvents.MODIFY_COMMAND.register { command ->
            if (Chat.isSendingModCommand) return@register command
            ModifyCommandEventManager.runTasks(command)
        }
    }

    @ScopedRegistration
    @Defaults("filter = null")
    internal fun registerAnyChatEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component, matches: MatchResult?) -> Unit
    ): Pair<ChatEventManager.ChatEvent, GameEventManager.GameEvent> {
        val chatEvent = registerChatEvent(filter, priority, callback)
        val gameEvent = registerGameEvent(filter, priority, false) { text, _, matches ->
            callback(text, matches)
        }
        return Pair(chatEvent, gameEvent)
    }

    @ScopedRegistration
    @Defaults("filter = null")
    internal fun registerChatEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component, matches: MatchResult?) -> Unit
    ): ChatEventManager.ChatEvent {
        return registerAllowChatEvent(filter, priority) { text, matches ->
            callback(text, matches)
            true
        }
    }

    @ScopedRegistration
    @Defaults("filter = null", "isOverlay = false")
    internal fun registerGameEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        isOverlay: Boolean = false,
        callback: (text: Component, overlay: Boolean, matches: MatchResult?) -> Unit
    ): GameEventManager.GameEvent {
        return registerAllowGameEvent(filter, priority) { text, overlay, matches ->
            if (isOverlay != overlay) return@registerAllowGameEvent true

            callback(text, overlay, matches)
            true
        }
    }

    @ScopedRegistration
    @Defaults("filter = null")
    internal fun registerAllowChatEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component, matches: MatchResult?) -> Boolean
    ): ChatEventManager.ChatEvent {
        return ChatEventManager.ChatEvent(priority) { text ->
            if (filter != null) {
                val match = filter.find(text.toUnformattedString()) ?: return@ChatEvent true
                return@ChatEvent callback(text, match)
            }

            callback(text, null)
        }.register()
    }

    @ScopedRegistration
    @Defaults("filter = null")
    internal fun registerAllowGameEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (text: Component, overlay: Boolean, matches: MatchResult?) -> Boolean
    ): GameEventManager.GameEvent {
        return GameEventManager.GameEvent(priority) { text, overlay ->
            if (filter != null) {
                val match = filter.find(text.toUnformattedString()) ?: return@GameEvent true
                return@GameEvent callback(text, overlay, match)
            }

            callback(text, overlay, null)
        }.register()
    }
}
