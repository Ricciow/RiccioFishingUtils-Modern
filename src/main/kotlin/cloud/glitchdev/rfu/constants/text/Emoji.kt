package cloud.glitchdev.rfu.constants.text


data class EmojiData(
    val unicode: String,
    val aliases: List<String>,
    val customTriggers: List<String> = emptyList()
) {
    val primaryAlias: String get() = aliases.firstOrNull() ?: ""
    val displayName: String get() = Emoji.formatDisplayName(aliases.maxByOrNull { it.length } ?: primaryAlias)
    val triggers: List<String> get() = aliases.map { ":$it:" } + customTriggers
}

object Emoji {
    val EMOJIS: List<EmojiData> = listOf(
        // Non-Sea Creatures
        EmojiData("\uE100", listOf("dog"), listOf("(ᵔᴥᵔ)")),
        EmojiData("\uE101", listOf("goat")),
        EmojiData("\uE102", listOf("pleading_face", "pleadingface", "plead")),
        EmojiData("\uE11A", listOf("skull")),
        EmojiData("\uE11B", listOf("sob")),
        EmojiData("\uE11C", listOf("thumbsup")),
        EmojiData("\uE11D", listOf("eyes")),
        EmojiData("\uE11E", listOf("angry")),
        EmojiData("\uE11F", listOf("fire")),
        EmojiData("\uE120", listOf("scream")),
        EmojiData("\uE121", listOf("thumbsupcat")),
        EmojiData("\uE122", listOf("thumbsdown")),
        EmojiData("\uE125", listOf("hog")),
        EmojiData("\uE126", listOf("exploding_head")),
        EmojiData("\uE127", listOf("kaboom", "boom")),
        EmojiData("\uE128", listOf("carrot")),
        EmojiData("\uE129", listOf("shark")),
        EmojiData("\uE12A", listOf("fish")),
        EmojiData("\uE12B", listOf("face_holding_back_tears", "fhbt")),
        EmojiData("\uE12C", listOf("rolling_eyes")),

        // Sea Creatures
        EmojiData("\uE116", listOf("abyssal_miner", "abyssalminer", "miner")),
        EmojiData("\uE10E", listOf("alligator", "gator")),
        EmojiData("\uE10D", listOf("blue_ringed_octopus", "blueringedoctopus", "octopus")),
        EmojiData("\uE10A", listOf("fiery_scuttler", "fieryscuttler", "scuttler")),
        EmojiData("\uE10F", listOf("frog_prince", "frogprince", "prince")),
        EmojiData("\uE113", listOf("great_white_shark", "greatwhiteshark", "great_white", "greatwhite", "gw")),
        EmojiData("\uE114", listOf("grim_reaper", "grimreaper", "reaper", "grim")),
        EmojiData("\uE103", listOf("lord_jawbus", "lordjawbus", "jawbus", "jaw")),
        EmojiData("\uE111", listOf("nessie", "ness")),
        EmojiData("\uE115", listOf("phantom_fisher", "phantomfisher", "pfish")),
        EmojiData("\uE10B", listOf("plhlegblast", "plhleg")),
        EmojiData("\uE110", listOf("puddle_jumper", "puddlejumper", "puddle", "jumper")),
        EmojiData("\uE109", listOf("ragnarok", "rag")),
        EmojiData("\uE105", listOf("reindrake", "drake")),
        EmojiData("\uE112", listOf("the_loch_emperor", "thelochemperor", "loch_emperor", "lochemperor", "emperor", "emp")),
        EmojiData("\uE104", listOf("thunder", "thun")),
        EmojiData("\uE107", listOf("titanoboa", "boa")),
        EmojiData("\uE10C", listOf("water_hydra", "waterhydra", "hydra")),
        EmojiData("\uE106", listOf("wiki_tiki", "wikitiki", "tiki")),
        EmojiData("\uE108", listOf("yeti")),
        EmojiData("\uE123", listOf("giant_isopod", "isopod", "pod")),
        EmojiData("\uE124", listOf("silkbreeze", "silk")),
        EmojiData("\uE12D", listOf("aquamarine", "aquamarine_dye")),
        EmojiData("\uE12E", listOf("carmine", "carmine_dye")),
        EmojiData("\uE12F", listOf("midnight", "midnight_dye")),
        EmojiData("\uE130", listOf("treasure", "treasure_dye")),
    )

    val ALL: Map<String, String> = EMOJIS.flatMap { emoji ->
        emoji.triggers.map { it to emoji.unicode }
    }.toMap()

    val COLON_TRIGGERS: Map<String, String> = EMOJIS.flatMap { emoji ->
        emoji.aliases.map { ":$it:" to emoji.unicode }
    }.toMap()

    val CUSTOM_TRIGGERS: Map<String, String> = EMOJIS.flatMap { emoji ->
        emoji.customTriggers.map { it to emoji.unicode }
    }.toMap()

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }

    fun formatDisplayName(alias: String): String {
        return alias.replace('_', ' ')
            .split(' ')
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}
