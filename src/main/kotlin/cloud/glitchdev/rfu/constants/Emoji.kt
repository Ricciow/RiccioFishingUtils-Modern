package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects

object Emoji {
    val ALL = mapOf(
        "\uE001" to listOf("dog"),
        "\uE002" to listOf("goat"),
        "\uE003" to listOf("pleading_face", "pleadingface", "plead"),
        "\uE004" to listOf("lord_jawbus", "lordjawbus", "jawbus", "jaw"),
        "\uE005" to listOf("thunder", "thun"),
        "\uE006" to listOf("reindrake", "drake"),
        "\uE007" to listOf("wiki_tiki", "wikitiki", "tiki"),
        "\uE008" to listOf("titanoboa", "boa"),
        "\uE009" to listOf("yeti"),
        "\uE00A" to listOf("ragnarok", "rag"),
        "\uE00B" to listOf("fiery_scuttler", "fieryscuttler", "scuttler"),
        "\uE00C" to listOf("plhegblast", "plhleg"),
        "\uE00D" to listOf("water_hydra", "waterhydra", "hydra"),
        "\uE00E" to listOf("blue_ringed_octopus", "blueringedoctopus", "octopus"),
        "\uE00F" to listOf("alligator", "gator"),
        "\uE0F0" to listOf("frog_prince", "frogprince", "prince"),
        "\uE0F1" to listOf("puddle_jumper", "puddlejumper", "puddle", "jumper"),
        "\uE0F2" to listOf("nessie", "ness"),
        "\uE0F3" to listOf("the_loch_emperor", "thelochemperor", "loch_emperor", "lochemperor", "emperor", "emp"),
        "\uE0F4" to listOf("great_white_shark", "greatwhiteshark", "great_white", "greatwhite", "gw"),
        "\uE0F5" to listOf("grim_reaper", "grimreaper", "reaper"),
        "\uE0F6" to listOf("phantom_fisher", "phantomfisher", "pfish"),
        "\uE0F7" to listOf("abyssal_miner", "abyssalminer", "miner")
    ).flatMap { (unicode, aliases) ->
        aliases.map { ":$it:" to unicode.whiteText() }
    }.toMap()

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }
}
