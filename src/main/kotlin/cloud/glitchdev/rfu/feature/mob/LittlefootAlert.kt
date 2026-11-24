package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.connection.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.connection.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.entity.registerMobDetectEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.Sounds

@RFUFeature
object LittlefootAlert : Feature {
    private var lastEntities : Set<SkyblockEntity> = setOf()

    override fun onInitialize(scope: EventScope) {
        scope.registerMobDetectEvent { entities ->
            if(!OtherSettings.littlefoot.enabled) return@registerMobDetectEvent
            
            val littlefoots = entities.filter { !it.isRemoved() && it.sbName.contains("Littlefoot", ignoreCase = true) }.toSet()
            val newLittlefoots = littlefoots.minus(lastEntities)
            lastEntities = littlefoots

            newLittlefoots.forEach { entity ->
                OtherSettings.littlefoot.show("name" to entity.sbName) { !entity.isRemoved() }
            }

            if(newLittlefoots.isNotEmpty()) {
                if (OtherSettings.littlefoot.sound) {
                    Sounds.playSound("rfu:littlefoot", 1f, OtherSettings.littlefoot.volume)
                }
            }
        }

        scope.registerLocationEvent {
            lastEntities = emptySet()
        }

        scope.registerDisconnectEvent {
            lastEntities = emptySet()
        }
    }
}
