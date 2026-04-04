package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object RainAlert : Feature {
    private var hasRained = false
    private var alertSent = false

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if(!InkFishing.rainAlert) return@registerTickEvent

            if(World.island == FishingIslands.PARK) {
                val isRaining = mc.level?.isRaining ?: false

                if(isRaining) {
                    hasRained = true
                    alertSent = false
                } else if(hasRained && !alertSent) {
                    alertSent = true
                    Title.showTitle("§b§lRain Expired!", "§7Go to Vanessa!", 10, 20, 10)
                    Sounds.playSound("rfu:rare_sc", 1f, 1f)
                }
            }
        }
    }
}