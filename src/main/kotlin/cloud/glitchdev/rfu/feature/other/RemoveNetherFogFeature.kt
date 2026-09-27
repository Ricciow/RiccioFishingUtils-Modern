package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.FogEvents.registerFogEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object RemoveNetherFogFeature : Feature {
    override fun onInitialize() {
        registerFogEvent { fog, _, level, renderDistance, _ ->
            if (OtherSettings.removeNetherFog && World.isNether(level)) {
                fog.environmentalStart = renderDistance * 1.5f
                fog.environmentalEnd = renderDistance * 2.0f
                fog.skyEnd = maxOf(fog.skyEnd, renderDistance)
            }
        }
    }
}
