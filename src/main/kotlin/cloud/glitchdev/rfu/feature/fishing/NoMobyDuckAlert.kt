package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.BobberLiquidEvents.registerBobberLiquidEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object NoMobyDuckAlert : Feature {
    override fun onInitialize() {
        registerBobberLiquidEvent {
            if (!GeneralFishing.noMobyDuck.enabled) return@registerBobberLiquidEvent
            if (!World.isInSkyblock) return@registerBobberLiquidEvent

            if (!MobyDuckFeature.isActive) {
                GeneralFishing.noMobyDuck.show()
            }
        }
    }
}
