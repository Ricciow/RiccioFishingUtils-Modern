package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@RFUFeature
object PetLevelUpAlert : Feature {
    val LEVEL_UP_REGEX = """Your (.+) leveled up to level (\d+)!""".toExactRegex()

    override fun onInitialize() {
        registerGameEvent(LEVEL_UP_REGEX) { text, _, _ ->
            if (!OtherSettings.petLevelUpAlert) return@registerGameEvent

            val match = LEVEL_UP_REGEX.find(text.string) ?: return@registerGameEvent
            val leveledPetName = match.groupValues[1]
            val newLevel = match.groupValues[2].toIntOrNull() ?: return@registerGameEvent

            if (newLevel >= OtherSettings.petLevelUpMinLevel) {
                Title.showTitle(
                    "§a§lPET LEVEL UP!",
                    "§eYour §6$leveledPetName §eleveled up to level §6$newLevel§e!",
                    fadeIn = 10,
                    duration = 40,
                    fadeOut = 10
                )
            }
        }
    }
}