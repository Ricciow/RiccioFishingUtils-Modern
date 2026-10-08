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
    val textReplacement: String? = null
) {
    val primaryAlias: String get() = aliases.firstOrNull() ?: ""
    val text: String get() = textReplacement ?: ":$primaryAlias:"
    val displayName: String get() = Emoji.formatDisplayName(aliases.maxByOrNull { it.length } ?: primaryAlias)
    val triggers: List<String> get() = aliases.map { ":$it:" } + customTriggers
    val font: FontDescription.AtlasSprite? = sprite?.let {
        FontDescription.AtlasSprite(Identifier.withDefaultNamespace("gui"), Identifier.fromNamespaceAndPath("rfu", "emoji/$it"))
    }

    fun spriteStyle(style: Style): Style = style.withFont(font).withColor(ChatFormatting.WHITE)
        .withShadowColor(0).withBold(false).withItalic(false).withObfuscated(false)

    fun component(): MutableComponent = font?.let {
        Component.`object`(AtlasSprite(it.atlasId(), it.spriteId()), Component.literal(text))
            .setStyle(spriteStyle(Style.EMPTY).withFont(null))
    } ?: Component.literal(text)
}

object Emoji {
    val ICONS = listOf(
        EmojiData("github", listOf("github")),
        EmojiData("discord", listOf("discord")),
        EmojiData("patreon", listOf("patreon")),
    )

    val EMOJIS: List<EmojiData> = listOf(
        // Non-Sea Creatures
        EmojiData("dog", listOf("dog"), listOf("(ᵔᴥᵔ)")),
        EmojiData("goat", listOf("goat")),
        EmojiData("pleadingface", listOf("pleading_face", "pleadingface", "plead")),
        EmojiData("skull", listOf("skull")),
        EmojiData("sob", listOf("sob")),
        EmojiData("thumbsup", listOf("thumbsup")),
        EmojiData("eyes", listOf("eyes")),
        EmojiData("angry", listOf("angry")),
        EmojiData("fire", listOf("fire")),
        EmojiData("scream", listOf("scream")),
        EmojiData("thumbsupcat", listOf("thumbsupcat")),
        EmojiData("thumbsdown", listOf("thumbsdown")),
        EmojiData("hog", listOf("hog")),
        EmojiData("exploding_head", listOf("exploding_head")),
        EmojiData("kaboom", listOf("kaboom", "boom")),
        EmojiData("carrot", listOf("carrot")),
        EmojiData("shark", listOf("shark")),
        EmojiData("fish", listOf("fish")),
        EmojiData("face_holding_back_tears", listOf("face_holding_back_tears", "fhbt")),
        EmojiData("rolling_eyes", listOf("rolling_eyes")),

        // Sea Creatures
        EmojiData("abyssalminer", listOf("abyssal_miner", "abyssalminer", "miner")),
        EmojiData("alligator", listOf("alligator", "gator")),
        EmojiData("blueringedoctopus", listOf("blue_ringed_octopus", "blueringedoctopus", "octopus")),
        EmojiData("fieryscuttler", listOf("fiery_scuttler", "fieryscuttler", "scuttler")),
        EmojiData("frogprince", listOf("frog_prince", "frogprince", "prince")),
        EmojiData("greatwhiteshark", listOf("great_white_shark", "greatwhiteshark", "great_white", "greatwhite", "gw")),
        EmojiData("grimreaper", listOf("grim_reaper", "grimreaper", "reaper", "grim")),
        EmojiData("lordjawbus", listOf("lord_jawbus", "lordjawbus", "jawbus", "jaw")),
        EmojiData("nessie", listOf("nessie", "ness")),
        EmojiData("phantomfisher", listOf("phantom_fisher", "phantomfisher", "pfish")),
        EmojiData("plhlegblast", listOf("plhlegblast", "plhleg")),
        EmojiData("puddlejumper", listOf("puddle_jumper", "puddlejumper", "puddle", "jumper")),
        EmojiData("ragnarok", listOf("ragnarok", "rag")),
        EmojiData("reindrake", listOf("reindrake", "drake")),
        EmojiData("lochemperor", listOf("the_loch_emperor", "thelochemperor", "loch_emperor", "lochemperor", "emperor", "emp")),
        EmojiData("thunder", listOf("thunder", "thun")),
        EmojiData("titanoboa", listOf("titanoboa", "boa")),
        EmojiData("waterhydra", listOf("water_hydra", "waterhydra", "hydra")),
        EmojiData("wikitiki", listOf("wiki_tiki", "wikitiki", "tiki")),
        EmojiData("yeti", listOf("yeti")),
        EmojiData("giant_isopod", listOf("giant_isopod", "isopod", "pod")),
        EmojiData("silkbreeze", listOf("silkbreeze", "silk")),
        EmojiData("aquamarine_dye", listOf("aquamarine", "aquamarine_dye")),
        EmojiData("carmine_dye", listOf("carmine", "carmine_dye")),
        EmojiData("midnight_dye", listOf("midnight", "midnight_dye")),
        EmojiData("treasure_dye", listOf("treasure", "treasure_dye")),

        // Other
        EmojiData(null, listOf("boop"), listOf("Boop!"), "§d§lBoop!"),
        EmojiData(null, listOf("boo"), listOf("Boo!"), "§6§lBoo!"),
    )

    val ALL: Map<String, EmojiData> = EMOJIS.flatMap { emoji ->
        emoji.triggers.map { it to emoji }
    }.toMap()

    val COLON_TRIGGERS: Map<String, EmojiData> = (EMOJIS + ICONS).flatMap { emoji ->
        emoji.aliases.map { ":$it:" to emoji }
    }.toMap()

    val CUSTOM_TRIGGERS: Map<String, EmojiData> = EMOJIS.flatMap { emoji ->
        emoji.customTriggers.map { it to emoji }
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
