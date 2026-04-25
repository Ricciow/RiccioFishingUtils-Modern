package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.universal.utils.toFormattedString
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.network.chat.Component

@AutoRegister
object ChatEvents : RegisteredEvent {
    override fun register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            if (Chat.isSendingModMessage) return@register true
            val result = ChatEventManager.runTasks(message)
            if(!result) RFULogger.dev("Chat message was hid: ${message.toFormattedString()}")
            result
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (Chat.isSendingModMessage) return@register true
            val result = GameEventManager.runTasks(message, overlay)
            if(!result) RFULogger.dev("Game message was hid: ${message.toFormattedString()}")
            result
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            val result = SendChatEventManager.runTasks(message)
            if(!result) RFULogger.dev("Sent message was cancelled: $message")
            result
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { command ->
            if (Chat.isSendingModCommand) return@register true
            val result = SendCommandEventManager.runTasks(command)
            if(!result) RFULogger.dev("Sent commnad was cancelled: $command")
            result
        }
    }

    fun registerSendChatEvent(
        priority: Int = 20,
        callback: (message: String) -> Boolean
    ) : SendChatEventManager.SendChatEvent {
        return SendChatEventManager.register(priority, callback)
    }

    fun registerSendCommandEvent(
        priority: Int = 20,
        callback: (message: String) -> Boolean
    ) : SendCommandEventManager.SendCommandEvent {
        return SendCommandEventManager.register(priority, callback)
    }

    fun registerAnyChatEvent(
        filter: Regex? = null,
        priority: Int = 20,
        callback: (text: Component, matches: MatchResult?) -> Unit
    ): Pair<ChatEventManager.ChatEvent, GameEventManager.GameEvent> {
        val chatEvent = registerChatEvent(filter, priority, callback)
        val gameEvent = registerGameEvent(filter, priority, false) { text, _, matches ->
            callback(text, matches)
        }
        return Pair(chatEvent, gameEvent)
    }

    fun registerChatEvent(
        filter: Regex? = null,
        priority: Int = 20,
        callback: (text: Component, matches: MatchResult?) -> Unit
    ): ChatEventManager.ChatEvent {
        return registerAllowChatEvent(filter, priority) { text, matches ->
            callback(text, matches)
            true
        }
    }

    fun registerGameEvent(
        filter: Regex? = null,
        priority: Int = 20,
        isOverlay: Boolean = false,
        callback: (text: Component, overlay: Boolean, matches: MatchResult?) -> Unit
    ): GameEventManager.GameEvent {
        return registerAllowGameEvent(filter, priority) { text, overlay, matches ->
            if (isOverlay != overlay) return@registerAllowGameEvent true

            callback(text, overlay, matches)
            true
        }
    }

    fun registerAllowChatEvent(
        filter: Regex? = null,
        priority: Int = 20,
        callback: (text: Component, matches: MatchResult?) -> Boolean
    ): ChatEventManager.ChatEvent {
        return ChatEventManager.register(priority) { text ->
            if (filter != null) {
                val match = filter.find(text.toUnformattedString()) ?: return@register true
                return@register callback(text, match)
            }

            return@register callback(text, null)
        }
    }

    fun registerAllowGameEvent(
        filter: Regex? = null,
        priority: Int = 20,
        callback: (text: Component, overlay: Boolean, matches: MatchResult?) -> Boolean
    ): GameEventManager.GameEvent {
        return GameEventManager.register(priority) { text, overlay ->
            if (filter != null) {
                val match = filter.find(text.toUnformattedString()) ?: return@register true
                return@register callback(text, overlay, match)
            }

            return@register callback(text, overlay, null)
        }
    }

    object ChatEventManager : AbstractEventManager<(text: Component) -> Boolean, ChatEventManager.ChatEvent>() {
        override val runTasks: (text: Component) -> Boolean = { text ->
            var result = true
            safeExecution {
                for (event in tasks) if (!event.callback(text)) result = false
            }
            result
        }

        fun register(priority: Int = 20, callback: (text: Component) -> Boolean): ChatEvent {
            return ChatEvent(priority, callback).register()
        }

        class ChatEvent(
            priority: Int = 20,
            callback: (text: Component) -> Boolean
        ) : ManagedTask<(text: Component) -> Boolean, ChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object GameEventManager : AbstractEventManager<(text: Component, overlay: Boolean) -> Boolean, GameEventManager.GameEvent>() {
        override val runTasks: (text: Component, overlay: Boolean) -> Boolean = { text, overlay ->
            var result = true
            safeExecution {
                for (event in tasks) if (!event.callback(text, overlay)) result = false
            }
            result
        }

        fun register(priority: Int = 20, callback: (text: Component, overlay: Boolean) -> Boolean): GameEvent {
            return GameEvent(priority, callback).register()
        }

        class GameEvent(
            priority: Int = 20,
            callback: (text: Component, overlay: Boolean) -> Boolean
        ) : ManagedTask<(text: Component, overlay: Boolean) -> Boolean, GameEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object SendChatEventManager : AbstractEventManager<(message: String) -> Boolean, SendChatEventManager.SendChatEvent>() {
        override val runTasks: (message: String) -> Boolean = { message ->
            var result = true
            safeExecution {
                for (event in tasks) if (!event.callback(message)) result = false
            }
            result
        }

        fun register(priority: Int = 20, callback: (message: String) -> Boolean): SendChatEvent {
            return SendChatEvent(priority, callback).register()
        }

        class SendChatEvent(
            priority: Int = 20,
            callback: (message: String) -> Boolean
        ) : ManagedTask<(message: String) -> Boolean, SendChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object SendCommandEventManager : AbstractEventManager<(command: String) -> Boolean, SendCommandEventManager.SendCommandEvent>() {
        override val runTasks: (command: String) -> Boolean = { command ->
            var result = true
            safeExecution {
                for (event in tasks) if (!event.callback(command)) result = false
            }
            result
        }

        fun register(priority: Int = 20, callback: (command: String) -> Boolean): SendCommandEvent {
            return SendCommandEvent(priority, callback).register()
        }

        class SendCommandEvent(
            priority: Int = 20,
            callback: (command: String) -> Boolean
        ) : ManagedTask<(command: String) -> Boolean, SendCommandEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}