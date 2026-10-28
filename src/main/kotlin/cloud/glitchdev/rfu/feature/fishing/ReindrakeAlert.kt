package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.JerryFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object ReindrakeAlert : Feature {
    private val REINDRAKE_REGEX = Regex("^WOAH! (?:\\[.*?\\] )?(\\w+) summoned (a|TWO) Reindrakes? from the depths!$")

    override fun onInitialize() {
        registerGameEvent(REINDRAKE_REGEX) { _, _, match ->
            if (match == null || !JerryFishing.reindrake.enabled) return@registerGameEvent
            val count = match.groupValues[2]
            JerryFishing.reindrake.show("reindrakes" to if (count == "TWO") "TWO REINDRAKES" else "REINDRAKE")
        }
    }
}
