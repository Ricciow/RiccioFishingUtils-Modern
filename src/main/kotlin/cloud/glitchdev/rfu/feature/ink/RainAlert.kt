package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object RainAlert : Feature {
    private var hasRained = false
    private var alertSent = false

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if(!InkFishing.rain.enabled) return@registerTickEvent

            if(World.island == FishingIslands.PARK) {
                val isRaining = mc.level?.isRaining ?: false

                if(isRaining) {
                    hasRained = true
                    alertSent = false
                } else if(hasRained && !alertSent) {
                    alertSent = true
                    InkFishing.rain.show()
                    if (InkFishing.rain.sound) {
                        Sounds.playSound("rfu:rain_expired", 1f, InkFishing.rain.volume)
                    }
                }
            }
        }
    }
}
