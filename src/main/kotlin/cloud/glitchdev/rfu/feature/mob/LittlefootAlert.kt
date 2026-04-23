package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title

@RFUFeature
object LittlefootAlert : Feature {
    private var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize() {
        registerMobDetectEvent { entities ->
            if(!OtherSettings.littlefootAlert) return@registerMobDetectEvent
            
            val littlefoots = entities.filter { it.sbName.contains("Littlefoot", ignoreCase = true) }.toSet()
            val newLittlefoots = littlefoots.minus(lastEntities)
            lastEntities = littlefoots

            newLittlefoots.forEach { entity ->
                Title.showTitle("§6§l[§fα§6§l] §3§l${entity.sbName} §6§l[§fα§6§l]") { !entity.isRemoved() }
            }

            if(newLittlefoots.isNotEmpty()) {
                if (OtherSettings.littlefootSound) {
                    Sounds.playSound("rfu:littlefoot", 1f, OtherSettings.littlefootVolume)
                }
            }
        }
    }
}
