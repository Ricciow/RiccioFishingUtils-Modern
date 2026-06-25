package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import gg.essential.universal.utils.toUnformattedString

@RFUFeature
object RodTimerSound : Feature {
    private var lastRodTime = -1f

    override fun onInitialize() {
        registerTickEvent(0, 2) { _ ->
            if (!GeneralFishing.rodTimerSound) return@registerTickEvent

            val text = RodTimer.timer?.name?.toUnformattedString()

            val currentRodTime = if (text == "!!!") {
                0f
            } else {
                text?.toFloatOrNull() ?: -1f
            }

            if (currentRodTime != -1f) {
                if (currentRodTime != lastRodTime) {
                    val progress = 1f - (currentRodTime / 3.0f).coerceIn(0f, 1f)
                    
                    val pitch = 0.5f + progress * 1.5f
                    
                    Sounds.playSound("rfu:rod_timer", pitch, 1f)
                }
            }

            lastRodTime = currentRodTime
        }
    }
}
