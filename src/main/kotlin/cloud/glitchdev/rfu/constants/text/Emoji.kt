package cloud.glitchdev.rfu.constants.text


object Emoji {
    val EMOJIS = mapOf(
        // Non-Sea Creatures
        "\uE100" to listOf("dog"),
        "\uE101" to listOf("goat"),
        "\uE102" to listOf("pleading_face", "pleadingface", "plead"),
        "\uE11A" to listOf("skull"),
        "\uE11B" to listOf("sob"),
        "\uE11C" to listOf("thumbsup"),
        "\uE11D" to listOf("eyes"),
        "\uE11E" to listOf("angry"),
        "\uE11F" to listOf("fire"),
        "\uE120" to listOf("scream"),
        "\uE121" to listOf("thumbsupcat"),
        "\uE122" to listOf("thumbsdown"),

        // Sea Creatures
        "\uE116" to listOf("abyssal_miner", "abyssalminer", "miner"),
        "\uE10E" to listOf("alligator", "gator"),
        "\uE10D" to listOf("blue_ringed_octopus", "blueringedoctopus", "octopus"),
        "\uE10A" to listOf("fiery_scuttler", "fieryscuttler", "scuttler"),
        "\uE10F" to listOf("frog_prince", "frogprince", "prince"),
        "\uE113" to listOf("great_white_shark", "greatwhiteshark", "great_white", "greatwhite", "gw"),
        "\uE114" to listOf("grim_reaper", "grimreaper", "reaper", "grim"),
        "\uE103" to listOf("lord_jawbus", "lordjawbus", "jawbus", "jaw"),
        "\uE111" to listOf("nessie", "ness"),
        "\uE115" to listOf("phantom_fisher", "phantomfisher", "pfish"),
        "\uE10B" to listOf("plhlegblast", "plhleg"),
        "\uE110" to listOf("puddle_jumper", "puddlejumper", "puddle", "jumper"),
        "\uE109" to listOf("ragnarok", "rag"),
        "\uE105" to listOf("reindrake", "drake"),
        "\uE112" to listOf("the_loch_emperor", "thelochemperor", "loch_emperor", "lochemperor", "emperor", "emp"),
        "\uE104" to listOf("thunder", "thun"),
        "\uE107" to listOf("titanoboa", "boa"),
        "\uE10C" to listOf("water_hydra", "waterhydra", "hydra"),
        "\uE106" to listOf("wiki_tiki", "wikitiki", "tiki"),
        "\uE108" to listOf("yeti")

        //\uE1FF next
    )

    val ALL = EMOJIS.flatMap { (unicode, aliases) ->
        aliases.map { ":$it:" to unicode.whiteText() }
    }.toMap()

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }

    @JvmStatic
    fun containsAnEmoji(string: String) : Boolean {
        return string.any { char -> char.toString() in EMOJIS }
    }
}
