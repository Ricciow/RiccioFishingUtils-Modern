package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import gg.essential.universal.utils.toUnformattedString

@RFUFeature
object HideMessages : Feature {
    override fun onInitialize() {
        registerAllowGameEvent { message, _, _ ->
            if (!OtherSettings.hideMessages) return@registerAllowGameEvent true

            val text = message.toUnformattedString()
            OtherSettings.hiddenMessageTypes.none { it.matches(text) }
        }
    }
}
