package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.RareScSettings
import cloud.glitchdev.rfu.config.categories.RareScSettings.HEALTH_BAR_REGEX
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.bossbar.BossHealthBarDisplay
import cloud.glitchdev.rfu.data.mob.MobManager

@RFUFeature
object BossHealthBar : Feature {
    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!RareScSettings.bossHealthBars) {
                BossHealthBarDisplay.updateEntities(emptySet())
                return@registerMobDetectEvent
            }
            val filteredEntities = entities.filter { HEALTH_BAR_REGEX.matches(it.sbName) }
            BossHealthBarDisplay.updateEntities(filteredEntities.toSet())
            if(RareScSettings.boostPollingRate) MobManager.boostDetectionRate(filteredEntities.isNotEmpty())
        }
    }
}