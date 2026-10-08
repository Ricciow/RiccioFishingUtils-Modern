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
    private val legacyAliases = mapOf(
        '\uE100' to ":dog:",
        '\uE101' to ":goat:",
        '\uE102' to ":pleading_face:",
        '\uE103' to ":lord_jawbus:",
        '\uE104' to ":thunder:",
        '\uE105' to ":reindrake:",
        '\uE106' to ":wiki_tiki:",
        '\uE107' to ":titanoboa:",
        '\uE108' to ":yeti:",
        '\uE109' to ":ragnarok:",
        '\uE10A' to ":fiery_scuttler:",
        '\uE10B' to ":plhlegblast:",
        '\uE10C' to ":water_hydra:",
        '\uE10D' to ":blue_ringed_octopus:",
        '\uE10E' to ":alligator:",
        '\uE10F' to ":frog_prince:",
        '\uE110' to ":puddle_jumper:",
        '\uE111' to ":nessie:",
        '\uE112' to ":the_loch_emperor:",
        '\uE113' to ":great_white_shark:",
        '\uE114' to ":grim_reaper:",
        '\uE115' to ":phantom_fisher:",
        '\uE116' to ":abyssal_miner:",
        '\uE117' to ":github:",
        '\uE118' to ":discord:",
        '\uE119' to ":patreon:",
        '\uE11A' to ":skull:",
        '\uE11B' to ":sob:",
        '\uE11C' to ":thumbsup:",
        '\uE11D' to ":eyes:",
        '\uE11E' to ":angry:",
        '\uE11F' to ":fire:",
        '\uE120' to ":scream:",
        '\uE121' to ":thumbsupcat:",
        '\uE122' to ":thumbsdown:",
        '\uE123' to ":giant_isopod:",
        '\uE124' to ":silkbreeze:",
        '\uE125' to ":hog:",
        '\uE126' to ":exploding_head:",
        '\uE127' to ":kaboom:",
        '\uE128' to ":carrot:",
        '\uE129' to ":shark:",
        '\uE12A' to ":fish:",
        '\uE12B' to ":face_holding_back_tears:",
        '\uE12C' to ":rolling_eyes:",
        '\uE12D' to ":aquamarine:",
        '\uE12E' to ":carmine:",
        '\uE12F' to ":midnight:",
        '\uE130' to ":treasure:",
    )

    fun convertLegacyEmojis(text: String): String = buildString(text.length) {
        for (char in text) append(legacyAliases[char] ?: char.toString())
    }

    val ICONS = listOf(
        EmojiData("github", listOf("github"), height = 16, ascent = 11),
        EmojiData("discord", listOf("discord"), height = 16, ascent = 11),
        EmojiData("patreon", listOf("patreon"), height = 16, ascent = 11),
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
        EmojiData("fieryscuttler", listOf("fiery_scuttler", "fieryscuttler", "scuttler"), height = 16, ascent = 12),
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
        EmojiData("titanoboa", listOf("titanoboa", "boa"), height = 16, ascent = 12),
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

    private val sprites = (EMOJIS + ICONS).mapNotNull { emoji ->
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
