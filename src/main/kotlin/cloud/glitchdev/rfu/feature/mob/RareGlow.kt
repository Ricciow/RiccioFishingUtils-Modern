package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object RareGlow : Feature {
    override fun onInitialize(scope: EventScope) {
        scope.registerMobDetectEvent { entities ->
            entities.forEach { entity ->
                val isRare = RARE_SC_REGEX.matches(entity.sbName)
                if (SeaCreatureConfig.rareScGlow && isRare) {
                    entity.setGlowing(true)
                } else if (entity.isGlowing()) {
                    entity.setGlowing(false)
                }
            }
        }
    }
}
