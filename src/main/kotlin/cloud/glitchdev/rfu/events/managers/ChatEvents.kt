package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.registration.Defaults
import cloud.glitchdev.rfu.events.registration.ScopedRegistration
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
    private var lastHandledChatMessage: Component? = null
    private var lastHandledGameMessage: Component? = null
    private var lastHandledGameOverlay = false

    override fun register() {
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

    fun registerSendChatEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (message: String) -> Boolean
    ) : SendChatEventManager.SendChatEvent {
        return SendChatEventManager.register(priority, callback)
    }

    fun registerSendCommandEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (message: String) -> Boolean
    ) : SendCommandEventManager.SendCommandEvent {
        return SendCommandEventManager.register(priority, callback)
    }

    fun registerModifyChatEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (message: String) -> String
    ): ModifyChatEventManager.ModifyChatEvent {
        return ModifyChatEventManager.register(priority, callback)
    }

    fun registerModifyCommandEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (command: String) -> String
    ): ModifyCommandEventManager.ModifyCommandEvent {
        return ModifyCommandEventManager.register(priority, callback)
    }

    @ScopedRegistration
    @Defaults("filter = null")
    fun registerAnyChatEvent(
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
    fun registerChatEvent(
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
    fun registerGameEvent(
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
    fun registerAllowChatEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
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

    @ScopedRegistration
    @Defaults("filter = null")
    fun registerAllowGameEvent(
        filter: Regex? = null,
        priority: Int = DEFAULT_EVENT_PRIORITY,
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

    @EventManager(register = false, generateRegistration = false)
    object ChatEventManager : AbstractEventManager<(text: Component) -> Boolean, ChatEventManager.ChatEvent>() {
        override val runTasks: (text: Component) -> Boolean = { text ->
            var result = true
            for (event in tasks) {
                safeExecution {
                    if (!event.callback(text)) result = false
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (text: Component) -> Boolean): ChatEvent {
            return ChatEvent(priority, callback).register()
        }

        class ChatEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (text: Component) -> Boolean
        ) : ManagedTask<(text: Component) -> Boolean, ChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false, generateRegistration = false)
    object GameEventManager : AbstractEventManager<(text: Component, overlay: Boolean) -> Boolean, GameEventManager.GameEvent>() {
        override val runTasks: (text: Component, overlay: Boolean) -> Boolean = { text, overlay ->
            var result = true
            for (event in tasks) {
                safeExecution {
                    if (!event.callback(text, overlay)) result = false
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (text: Component, overlay: Boolean) -> Boolean): GameEvent {
            return GameEvent(priority, callback).register()
        }

        class GameEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (text: Component, overlay: Boolean) -> Boolean
        ) : ManagedTask<(text: Component, overlay: Boolean) -> Boolean, GameEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object SendChatEventManager : AbstractEventManager<(message: String) -> Boolean, SendChatEventManager.SendChatEvent>() {
        override val runTasks: (message: String) -> Boolean = { message ->
            var result = true
            for (event in tasks) {
                safeExecution {
                    if (!event.callback(message)) result = false
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (message: String) -> Boolean): SendChatEvent {
            return SendChatEvent(priority, callback).register()
        }

        class SendChatEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (message: String) -> Boolean
        ) : ManagedTask<(message: String) -> Boolean, SendChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object ModifyChatEventManager : AbstractEventManager<(message: String) -> String, ModifyChatEventManager.ModifyChatEvent>() {
        override val runTasks: (message: String) -> String = { message ->
            var result = message
            for (event in tasks) {
                safeExecution(mainThread = false) {
                    result = event.callback(result)
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (message: String) -> String): ModifyChatEvent {
            return ModifyChatEvent(priority, callback).register()
        }

        class ModifyChatEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (message: String) -> String
        ) : ManagedTask<(message: String) -> String, ModifyChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object SendCommandEventManager : AbstractEventManager<(command: String) -> Boolean, SendCommandEventManager.SendCommandEvent>() {
        override val runTasks: (command: String) -> Boolean = { command ->
            var result = true
            for (event in tasks) {
                safeExecution {
                    if (!event.callback(command)) result = false
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (command: String) -> Boolean): SendCommandEvent {
            return SendCommandEvent(priority, callback).register()
        }

        class SendCommandEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (command: String) -> Boolean
        ) : ManagedTask<(command: String) -> Boolean, SendCommandEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object ModifyCommandEventManager : AbstractEventManager<(command: String) -> String, ModifyCommandEventManager.ModifyCommandEvent>() {
        override val runTasks: (command: String) -> String = { command ->
            var result = command
            for (event in tasks) {
                safeExecution(mainThread = false) {
                    result = event.callback(result)
                }
            }
            result
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (command: String) -> String): ModifyCommandEvent {
            return ModifyCommandEvent(priority, callback).register()
        }

        class ModifyCommandEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (command: String) -> String
        ) : ManagedTask<(command: String) -> String, ModifyCommandEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
