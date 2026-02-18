package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.GeneralFishing.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.managers.MobDetectEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.Title

@RFUFeature
object RareAlert : Feature {
    var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!GeneralFishing.detectionAlert) return@registerMobDetectEvent
            val entities = entities.filter { RARE_SC_REGEX.matches(it.sbName) }.toSet()
            val newEntities = entities.minus(lastEntities)
            lastEntities = entities

            val seenEntities = mutableSetOf<String>()

            newEntities.filter { entity ->
                val result = !seenEntities.contains(entity.sbName)
                seenEntities.add(entity.sbName)
                result
            }.forEach { entity ->
                Title.showTitle("§6§l[§fα§6§l] §3§l${entity.sbName} §6§l[§fα§6§l]")
            }
        }
    }
}