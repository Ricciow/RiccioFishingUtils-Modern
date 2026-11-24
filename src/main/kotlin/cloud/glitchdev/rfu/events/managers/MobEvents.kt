package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.data.mob.SkyblockEntity

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

    @EventManager(register = false)
    object MobDetectEventManager : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDetectEventManager.MobDetectEvent>() {
        override val runTasks: (Set<SkyblockEntity>) -> Unit = { mobs ->
            safeExecution {
                tasks.forEach { task -> task.callback(mobs) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Set<SkyblockEntity>) -> Unit): MobDetectEvent {
            return MobDetectEvent(priority, callback).register()
        }

        class MobDetectEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (Set<SkyblockEntity>) -> Unit
        ) : ManagedTask<(Set<SkyblockEntity>) -> Unit, MobDetectEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object MobDisposeEventManager : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDisposeEventManager.MobDisposeEvent>() {
        override val runTasks: (Set<SkyblockEntity>) -> Unit = { mob ->
            safeExecution {
                tasks.forEach { task -> task.callback(mob) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Set<SkyblockEntity>) -> Unit): MobDisposeEvent {
            return MobDisposeEvent(priority, callback).register()
        }

        class MobDisposeEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (Set<SkyblockEntity>) -> Unit
        ) : ManagedTask<(Set<SkyblockEntity>) -> Unit, MobDisposeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object MobDeathEventManager : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDeathEventManager.MobDeathEvent>() {
        override val runTasks: (Set<SkyblockEntity>) -> Unit = { mob ->
            safeExecution {
                tasks.forEach { task -> task.callback(mob) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Set<SkyblockEntity>) -> Unit): MobDeathEvent {
            return MobDeathEvent(priority, callback).register()
        }

        class MobDeathEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (Set<SkyblockEntity>) -> Unit
        ) : ManagedTask<(Set<SkyblockEntity>) -> Unit, MobDeathEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object MobUpdateEventManager : AbstractEventManager<(SkyblockEntity) -> Unit, MobUpdateEventManager.MobUpdateEvent>() {
        override val runTasks: (SkyblockEntity) -> Unit = { mob ->
            safeExecution {
                tasks.forEach { task -> task.callback(mob) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (SkyblockEntity) -> Unit): MobUpdateEvent {
            return MobUpdateEvent(priority, callback).register()
        }

        class MobUpdateEvent(
            priority: Int = DEFAULT_EVENT_PRIORITY,
            callback: (SkyblockEntity) -> Unit
        ) : ManagedTask<(SkyblockEntity) -> Unit, MobUpdateEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
