package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.BobberLiquidEvents.registerBobberLiquidEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object NoRainAlert : Feature {
    override fun onInitialize() {
        registerBobberLiquidEvent {
            if (!InkFishing.noRain.enabled) return@registerBobberLiquidEvent
            if (!World.isInSkyblock || World.island != FishingIslands.PARK) return@registerBobberLiquidEvent

            if (mc.level?.isRaining == false) {
                InkFishing.noRain.show()
            }
        }
    }
}
