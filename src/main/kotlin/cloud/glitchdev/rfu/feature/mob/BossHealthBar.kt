package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.HEALTH_BAR_REGEX
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.entity.registerMobDeathEvent
import cloud.glitchdev.rfu.events.managers.entity.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.entity.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.entity.registerMobUpdateEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.bossbar.BossHealthBarDisplay

@RFUFeature
object BossHealthBar : Feature {
    override fun onInitialize(scope: EventScope) {
        scope.registerMobDetectEvent { entities ->
            if (!SeaCreatureConfig.bossHealthBars) {
                BossHealthBarDisplay.updateEntities(emptySet())
                return@registerMobDetectEvent
            }
            val filteredEntities = entities.filter { !it.isRemoved() && HEALTH_BAR_REGEX.matches(it.sbName) }
            BossHealthBarDisplay.updateEntities(filteredEntities.toSet())
        }

        scope.registerMobDeathEvent { entities ->
            if (BossHealthBarDisplay.entities.removeAll(entities)) {
                BossHealthBarDisplay.updateState()
            }
        }

        scope.registerMobDisposeEvent { entities ->
            if (BossHealthBarDisplay.entities.removeAll(entities)) {
                BossHealthBarDisplay.updateState()
            }
        }

        scope.registerMobUpdateEvent { entity ->
            if (BossHealthBarDisplay.entities.contains(entity)) {
                BossHealthBarDisplay.updateState()
            }
        }
    }
}