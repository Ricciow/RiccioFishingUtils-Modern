package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.PetEvents.PetUpdateEventManager
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.parseHealthValue

@RFUFeature
object GdragAlert : Feature {
    private val alertedEntities = mutableSetOf<SkyblockEntity>()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if (!SeaCreatureConfig.goldenDragonAlert) return@registerMobDetectEvent

            val petName = PetUpdateEventManager.currentPetName
            val hasGdrag = petName == "Golden Dragon"

            entities.forEach { entity ->
                if (!SeaCreatureConfig.RARE_SC_REGEX.matches(entity.sbName)) return@forEach
                if (alertedEntities.contains(entity)) return@forEach

                val health = entity.health.parseHealthValue()
                val maxHealth = entity.maxHealth.parseHealthValue()
                if (maxHealth == 0) return@forEach

                val healthPercentage = (health.toDouble() / maxHealth.toDouble()) * 100

                if (healthPercentage <= SeaCreatureConfig.gdragAlertThreshold) {
                    if (!hasGdrag) {
                        alertedEntities.add(entity)
                        Title.showTitle("§c§lNO G-DRAGON!", "§eEquip your Golden Dragon!", fadeIn = 5, duration = 40, fadeOut = 5)
                        if (SeaCreatureConfig.goldenDragonSound) {
                            Sounds.playSound("rfu:gdrag_alert", 1f, SeaCreatureConfig.goldenDragonVolume)
                        }
                    }
                }
            }
        }

        registerMobDisposeEvent { entities ->
            alertedEntities.removeAll(entities)
        }
    }
}
