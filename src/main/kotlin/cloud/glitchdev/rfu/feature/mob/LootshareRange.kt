package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.MobManager

@RFUFeature
object LootshareRange : Feature {
    var RARE_SC_REGEX : Regex = GeneralFishing.rareSC.joinToString("|").toRegex()

    override fun onInitialize() {
        registerTickEvent(1, 10) { _ ->
            if(!GeneralFishing.lootshareRange) return@registerTickEvent
            val entities = MobManager.findSbEntities(RARE_SC_REGEX)
            entities.forEach { entity ->
                entity.registerLsRange()
            }
        }
    }
}