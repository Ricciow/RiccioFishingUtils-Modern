package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY

object MobEvents {
    fun registerMobDetectEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Set<SkyblockEntity>) -> Unit
    ): MobDetectEventManager.MobDetectEvent {
        return MobDetectEventManager.register(priority, callback)
    }

    fun registerMobDisposeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Set<SkyblockEntity>) -> Unit
    ): MobDisposeEventManager.MobDisposeEvent {
        return MobDisposeEventManager.register(priority, callback)
    }

    fun registerMobDeathEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Set<SkyblockEntity>) -> Unit
    ): MobDeathEventManager.MobDeathEvent {
        return MobDeathEventManager.register(priority, callback)
    }

    fun registerMobUpdateEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (SkyblockEntity) -> Unit
    ): MobUpdateEventManager.MobUpdateEvent {
        return MobUpdateEventManager.register(priority, callback)
    }
}
