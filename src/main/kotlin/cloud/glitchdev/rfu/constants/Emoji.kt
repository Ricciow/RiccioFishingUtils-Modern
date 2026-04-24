package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects

object Emoji {
    const val DOG_CHAR = "\uE001"

    val ALL = mapOf(
        ":dog:" to DOG_CHAR.whiteText(),
    )

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }
}
