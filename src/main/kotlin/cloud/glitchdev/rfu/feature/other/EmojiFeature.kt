package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.Emoji
import cloud.glitchdev.rfu.events.managers.ChatEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents

@RFUFeature
object EmojiFeature : Feature {
    override fun onInitialize() {
        ChatEvents.registerAllowChatEvent { message, _ ->
            if (!OtherSettings.emojis) return@registerAllowChatEvent true
            
            if (containsEmoji(message)) {
                val replaced = replaceEmojis(message)
                RFULogger.dev("Replacing emojis in chat message: ${message.toUnformattedString()} -> ${replaced.toUnformattedString()}")
                Chat.sendMessage(replaced)
                return@registerAllowChatEvent false
            }
            true
        }

        ChatEvents.registerAllowGameEvent { message, overlay, _ ->
            if (!OtherSettings.emojis || overlay) return@registerAllowGameEvent true

            if (containsEmoji(message)) {
                val replaced = replaceEmojis(message)
                RFULogger.dev("Replacing emojis in game message: ${message.toUnformattedString()} -> ${replaced.toUnformattedString()}")
                Chat.sendMessage(replaced)
                return@registerAllowGameEvent false
            }
            true
        }
    }

    /**
     * Checks if any emoji trigger exists within the component tree.
     */
    fun containsEmoji(component: Component): Boolean {
        val unformatted = component.toUnformattedString()
        val contains = Emoji.ALL.keys.any { unformatted.contains(it) }
        return contains
    }

    /**
     * Recursively replaces emoji triggers in a Component while preserving styles and siblings.
     */
    fun replaceEmojis(component: Component): MutableComponent {
        val contents = component.contents
        val result: MutableComponent = when (contents) {
            is PlainTextContents.LiteralContents -> {
                var text = contents.text
                Emoji.ALL.forEach { (trigger, replacement) ->
                    text = text.replace(trigger, replacement)
                }
                Component.literal(text)
            }
            is PlainTextContents -> {
                var text = try {
                    val field = contents.javaClass.getDeclaredField("text")
                    field.isAccessible = true
                    field.get(contents) as String
                } catch (e: Exception) {
                    contents.text()
                }
                
                Emoji.ALL.forEach { (trigger, replacement) ->
                    text = text.replace(trigger, replacement)
                }
                Component.literal(text)
            }
            is TranslatableContents -> {
                val args = contents.args.map { arg ->
                    if (arg is Component) replaceEmojis(arg) else {
                        var text = arg.toString()
                        Emoji.ALL.forEach { (trigger, replacement) ->
                            text = text.replace(trigger, replacement)
                        }
                        text
                    }
                }.toTypedArray()
                Component.translatable(contents.key, *args)
            }
            else -> {
                MutableComponent.create(contents)
            }
        }

        result.style = component.style
        
        component.siblings.forEach { sibling ->
            result.append(replaceEmojis(sibling))
        }

        return result
    }

    /**
     * Replaces ALL registered emoji triggers (e.g., :dog:) with their PUA characters in a String.
     */
    fun replaceEmojis(text: String?): String? {
        if (text == null || !OtherSettings.emojis) return text
        
        var result = text
        Emoji.ALL.forEach { (trigger, replacement) ->
            result = result?.replace(trigger, replacement)
        }
        return result
    }
}
