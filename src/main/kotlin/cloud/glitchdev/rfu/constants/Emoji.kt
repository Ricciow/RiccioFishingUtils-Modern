package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects

object Emoji {
    val EMOJIS = mapOf(
        // Non-Sea Creatures
        "\uE001" to listOf("dog"),
        "\uE002" to listOf("goat"),
        "\uE003" to listOf("pleading_face", "pleadingface", "plead"),
        "\uE0FB" to listOf("skull"),
        "\uE0FC" to listOf("sob"),
        "\uE0FD" to listOf("thumbsup"),

        // Sea Creatures
        "\uE0F7" to listOf("abyssal_miner", "abyssalminer", "miner"),
        "\uE00F" to listOf("alligator", "gator"),
        "\uE00E" to listOf("blue_ringed_octopus", "blueringedoctopus", "octopus"),
        "\uE00B" to listOf("fiery_scuttler", "fieryscuttler", "scuttler"),
        "\uE0F0" to listOf("frog_prince", "frogprince", "prince"),
        "\uE0F4" to listOf("great_white_shark", "greatwhiteshark", "great_white", "greatwhite", "gw"),
        "\uE0F5" to listOf("grim_reaper", "grimreaper", "reaper", "grim"),
        "\uE004" to listOf("lord_jawbus", "lordjawbus", "jawbus", "jaw"),
        "\uE0F2" to listOf("nessie", "ness"),
        "\uE0F6" to listOf("phantom_fisher", "phantomfisher", "pfish"),
        "\uE00C" to listOf("plhlegblast", "plhleg"),
        "\uE0F1" to listOf("puddle_jumper", "puddlejumper", "puddle", "jumper"),
        "\uE00A" to listOf("ragnarok", "rag"),
        "\uE006" to listOf("reindrake", "drake"),
        "\uE0F3" to listOf("the_loch_emperor", "thelochemperor", "loch_emperor", "lochemperor", "emperor", "emp"),
        "\uE005" to listOf("thunder", "thun"),
        "\uE008" to listOf("titanoboa", "boa"),
        "\uE00D" to listOf("water_hydra", "waterhydra", "hydra"),
        "\uE007" to listOf("wiki_tiki", "wikitiki", "tiki"),
        "\uE009" to listOf("yeti")

        //\uE0FE next
    )

    val ALL = EMOJIS.flatMap { (unicode, aliases) ->
        aliases.map { ":$it:" to unicode.whiteText() }
    }.toMap()

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }
}
