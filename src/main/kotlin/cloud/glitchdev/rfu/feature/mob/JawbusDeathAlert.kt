package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title

@RFUFeature
object JawbusDeathAlert : Feature {
    val JAWBUS_DEATH_REGEX = """☠ ([\w_]{3,16}) was killed by Lord Jawbus\.""".toRegex()

    override fun onInitialize() {
        registerGameEvent(JAWBUS_DEATH_REGEX) { _, _, matches ->
            if(!LavaFishing.diedJawbusAlert) return@registerGameEvent
            val username = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
            Title.showTitle("${TextColor.DARK_GRAY}☠ ${TextColor.GRAY}$username ${TextColor.DARK_GRAY}☠", "${TextColor.LIGHT_RED}was killed by Lord Jawbus")
        }
    }
}