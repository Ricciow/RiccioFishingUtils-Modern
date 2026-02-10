package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.GeneralFishing.HEALTH_BAR_REGEX
import cloud.glitchdev.rfu.events.managers.MobDetectEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

import cloud.glitchdev.rfu.gui.hud.elements.bossbar.BossHealthBarDisplay

@RFUFeature
object BossHealthBar : Feature {
    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!GeneralFishing.bossHealthBars) {
                BossHealthBarDisplay.updateEntities(emptySet())
                return@registerMobDetectEvent
            }
            val filteredEntities = entities.filter { HEALTH_BAR_REGEX.matches(it.sbName) }
            BossHealthBarDisplay.updateEntities(filteredEntities.toSet())
        }
    }
}