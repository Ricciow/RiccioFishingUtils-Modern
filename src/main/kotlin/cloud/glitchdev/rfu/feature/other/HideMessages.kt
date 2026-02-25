package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.MessageTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object HideMessages : Feature {
    val SC_MESSAGE_REGEX = SeaCreatures.entries.joinToString("|") { it.catchMessage }.toRegex()
    val DOUBLE_HOOK_REGEX = """Double Hook!|It's a Double Hook! Woot woot!|It's a Double Hook!""".toRegex()
    val AUTOPET_REGEX = """Autopet equipped your .+! VIEW RULE""".toRegex()
    val HYPE_REGEX = """Your Implosion hit \d+ enem(?:y|ies) for [\d.,]+ damage\.""".toRegex()

    override fun onInitialize() {
        registerAllowGameEvent(SC_MESSAGE_REGEX) { _, _, _ ->
            return@registerAllowGameEvent !(OtherSettings.hideMessages &&
                    OtherSettings.hiddenMessageTypes.contains(MessageTypes.CATCH))
        }

        registerAllowGameEvent(DOUBLE_HOOK_REGEX) { _, _, _ ->
            return@registerAllowGameEvent !(OtherSettings.hideMessages &&
                    OtherSettings.hiddenMessageTypes.contains(MessageTypes.CATCH))
        }

        registerAllowGameEvent(AUTOPET_REGEX) { _, _, _ ->
            return@registerAllowGameEvent !(OtherSettings.hideMessages &&
                    OtherSettings.hiddenMessageTypes.contains(MessageTypes.AUTOPET))
        }

        registerAllowGameEvent(HYPE_REGEX) { _, _, _ ->
            return@registerAllowGameEvent !(OtherSettings.hideMessages &&
                    OtherSettings.hiddenMessageTypes.contains(MessageTypes.HYPE))
        }
    }
}