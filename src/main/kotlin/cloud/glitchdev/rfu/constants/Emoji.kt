package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects

object Emoji {
    val ALL = mapOf(
        ":dog:" to "\uE001".whiteText(),
        ":goat:" to "\uE002".whiteText(),
        ":pleading_face:" to "\uE003".whiteText(),
        ":pleadingface:" to "\uE003".whiteText(),
        ":plead:" to "\uE003".whiteText(),
        ":lordjawbus:" to "\uE004".whiteText(),
        ":jawbus:" to "\uE004".whiteText(),
        ":jaw:" to "\uE004".whiteText(),
        ":thunder:" to "\uE005".whiteText(),
        ":reindrake:" to "\uE006".whiteText(),
        ":drake:" to "\uE006".whiteText(),
        ":wikitikis:" to "\uE007".whiteText(),
        ":tiki:" to "\uE007".whiteText(),
        ":titanoboa:" to "\uE008".whiteText(),
        ":boa:" to "\uE008".whiteText(),
        ":yeti:" to "\uE009".whiteText(),
        ":ragnarok:" to "\uE00A".whiteText(),
        ":rag:" to "\uE00A".whiteText(),
        ":fieryscuttler:" to "\uE00B".whiteText(),
        ":scuttler:" to "\uE00B".whiteText(),
        ":plhegblast:" to "\uE00C".whiteText(),
        ":plhleg:" to "\uE00C".whiteText(),
        ":waterhydra:" to "\uE00D".whiteText(),
        ":hydra:" to "\uE00D".whiteText(),
        ":blueringedoctopus:" to "\uE00E".whiteText(),
        ":octopus:" to "\uE00E".whiteText(),
        ":alligator:" to "\uE00F".whiteText(),
        ":gator:" to "\uE00F".whiteText(),
        ":frogprince:" to "\uE0F0".whiteText(),
        ":prince:" to "\uE0F0".whiteText(),
        ":puddlejumper:" to "\uE0F1".whiteText(),
        ":puddle:" to "\uE0F1".whiteText(),
        ":jumper:" to "\uE0F1".whiteText(),
        ":nessie:" to "\uE0F2".whiteText(),
        ":ness:" to "\uE0F2".whiteText(),
        ":thelochemeperor:" to "\uE0F3".whiteText(),
        ":lochemperor:" to "\uE0F3".whiteText(),
        ":emperor:" to "\uE0F3".whiteText(),
        ":emp:" to "\uE0F3".whiteText(),
        ":greatwhiteshark:" to "\uE0F4".whiteText(),
        ":greatwhite:" to "\uE0F4".whiteText(),
        ":gw:" to "\uE0F4".whiteText(),
        ":grimreaper:" to "\uE0F5".whiteText(),
        ":reaper:" to "\uE0F5".whiteText(),
        ":phantomfisher:" to "\uE0F6".whiteText(),
        ":pfisher:" to "\uE0F6".whiteText(),
        ":abyssalminer:" to "\uE0F7".whiteText(),
        ":miner:" to "\uE0F7".whiteText(),
    )

    fun String.whiteText() : String {
        return "${TextColor.WHITE}$this${TextEffects.RESET}"
    }
}
