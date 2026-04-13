package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.HEALTH_BAR_REGEX
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.bossbar.BossHealthBarDisplay
import cloud.glitchdev.rfu.data.mob.MobManager

@RFUFeature
object BossHealthBar : Feature {
    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!SeaCreatureConfig.bossHealthBars) {
                BossHealthBarDisplay.updateEntities(emptySet())
                return@registerMobDetectEvent
            }
            val filteredEntities = entities.filter { HEALTH_BAR_REGEX.matches(it.sbName) }
            BossHealthBarDisplay.updateEntities(filteredEntities.toSet())
            if(SeaCreatureConfig.boostPollingRate) MobManager.boostDetectionRate(filteredEntities.isNotEmpty())
        }
    }
}