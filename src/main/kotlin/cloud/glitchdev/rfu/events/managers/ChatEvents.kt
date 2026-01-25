package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text

@AutoRegister
object ChatEvents : RegisteredEvent {

    override fun register() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
            ChatEventManager.runTasks(message)
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            GameEventManager.runTasks(message, overlay)
        }
    }

    fun registerAnyChatEvent(
        filter: Regex? = null,
        priority: Int = 20,
        callback: (text: Text, matches: MatchResult?) -> Unit
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
        callback: (text: Text, matches: MatchResult?) -> Unit
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
        callback: (text: Text, overlay: Boolean, matches: MatchResult?) -> Unit
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
        callback: (text: Text, matches: MatchResult?) -> Boolean
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
        callback: (text: Text, overlay: Boolean, matches: MatchResult?) -> Boolean
    ): GameEventManager.GameEvent {
        return GameEventManager.register(priority) { text, overlay ->
            if (filter != null) {
                val match = filter.find(text.toUnformattedString()) ?: return@register true
                return@register callback(text, overlay, match)
            }

            return@register callback(text, overlay, null)
        }
    }

    object ChatEventManager : AbstractEventManager<(text: Text) -> Boolean, ChatEventManager.ChatEvent>() {
        fun runTasks(text: Text): Boolean {
            return tasks.fold(true) { acc, event -> acc && event.callback(text) }
        }

        fun register(priority: Int = 20, callback: (text: Text) -> Boolean): ChatEvent {
            return ChatEvent(priority, callback).register()
        }

        class ChatEvent(
            priority: Int = 20,
            callback: (text: Text) -> Boolean
        ) : ManagedTask<(text: Text) -> Boolean, ChatEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object GameEventManager : AbstractEventManager<(text: Text, overlay: Boolean) -> Boolean, GameEventManager.GameEvent>() {
        fun runTasks(text: Text, overlay: Boolean): Boolean {
            return tasks.fold(true) { acc, event -> acc && event.callback(text, overlay) }
        }

        fun register(priority: Int = 20, callback: (text: Text, overlay: Boolean) -> Boolean): GameEvent {
            return GameEvent(priority, callback).register()
        }

        class GameEvent(
            priority: Int = 20,
            callback: (text: Text, overlay: Boolean) -> Boolean
        ) : ManagedTask<(text: Text, overlay: Boolean) -> Boolean, GameEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}