package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.World


@RFUFeature
object RainAlert : Feature {

    override fun onInitialize() {

        var hasRained = false
        var alertSent = false

        registerTickEvent(interval = 20) {

            if(World.map == "The Park") {
                val isRaining = mc.level?.isRaining ?: false

                if(isRaining) {
                    hasRained = true
                    alertSent = false
                } else if(hasRained && !alertSent) {
                    alertSent = true
                    Title.showTitle("§b§lRain Expired!", "§7Go to Vanessa!")
                    Sounds.playSound("rfu:rare_sc", 1f, 1f)
                }
            }
        }
    }


}