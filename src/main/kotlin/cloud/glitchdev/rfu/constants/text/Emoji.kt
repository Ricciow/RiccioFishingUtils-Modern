package cloud.glitchdev.rfu.constants.text

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.objects.AtlasSprite
import net.minecraft.resources.Identifier

data class EmojiData(
    val sprite: String?,
    val aliases: List<String>,
    val customTriggers: List<String> = emptyList(),
    val textReplacement: String? = null,
    val height: Int = 8,
    val ascent: Int = 7,
    val messageReplacement: String? = null,
    val category: String = "general",
    val id: String = aliases.firstOrNull() ?: "",
) {
    val primaryAlias: String get() = aliases.firstOrNull() ?: id
    val text: String get() = textReplacement ?: messageReplacement ?: ":$primaryAlias:"
    val displayName: String get() = Emoji.formatDisplayName(aliases.maxByOrNull { it.length } ?: id)
    val triggers: List<String> get() = aliases.map { ":$it:" } + customTriggers
    val font: FontDescription.AtlasSprite? = sprite?.let {
        FontDescription.AtlasSprite(Identifier.withDefaultNamespace("gui"), Identifier.fromNamespaceAndPath("rfu", "emoji/$it"))
    }

    fun spriteStyle(style: Style): Style = style.withFont(font).withColor(ChatFormatting.WHITE)
        .withShadowColor(0).withBold(false).withItalic(false).withObfuscated(false)
        .withUnderlined(false).withStrikethrough(false)

    fun component(): MutableComponent = font?.let {
        Component.`object`(AtlasSprite(it.atlasId(), it.spriteId()), Component.literal(text))
            .setStyle(spriteStyle(Style.EMPTY).withFont(null))
    } ?: Component.literal(text)
}

object Emoji {
    private val allEmojis = EmojiPack.entries.map { it.emoji }
    val EMOJIS: List<EmojiData> = EmojiPack.entries.filter { it.listed }.map { it.emoji }

    private val sprites = allEmojis.mapNotNull { emoji ->
        emoji.font?.spriteId()?.let { it to emoji }
    }.toMap()

    @JvmStatic
    fun getSprite(spriteId: Identifier): EmojiData? = sprites[spriteId]

    fun icon(sprite: String): EmojiData = requireNotNull(
        getSprite(Identifier.fromNamespaceAndPath("rfu", "emoji/$sprite"))
    ) { "Unknown emoji sprite: $sprite" }

    val ALL: Map<String, EmojiData> = EMOJIS.flatMap { emoji ->
        emoji.triggers.map { it to emoji }
    }.toMap()

    val COLON_TRIGGERS: Map<String, EmojiData> = allEmojis.flatMap { emoji ->
        emoji.aliases.map { ":$it:" to emoji }
    }.toMap()

    val CUSTOM_TRIGGERS: Map<String, EmojiData> = allEmojis.flatMap { emoji ->
        val triggers = when {
            emoji.messageReplacement == null -> emoji.customTriggers
            emoji.textReplacement != null || emoji.font != null -> listOf(emoji.messageReplacement)
            else -> emptyList()
        }
        triggers.map { it to emoji }
    }.toMap()

    val MESSAGE_REPLACEMENTS: Map<String, EmojiData> = allEmojis.filter { it.messageReplacement != null }.flatMap { emoji ->
        emoji.triggers.map { it.lowercase() to emoji }
    }.toMap()

    fun formatDisplayName(alias: String): String {
        return alias.replace('_', ' ')
            .split(' ')
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}
