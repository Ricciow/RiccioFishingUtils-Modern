package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.GeneralFishing.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.managers.MobDetectEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object LootshareRange : Feature {
    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!GeneralFishing.lootshareRange) return@registerMobDetectEvent
            val entities = entities.filter { RARE_SC_REGEX.matches(it.sbName) }
            entities.forEach { entity ->
                entity.registerLsRange()
            }
        }
    }
}