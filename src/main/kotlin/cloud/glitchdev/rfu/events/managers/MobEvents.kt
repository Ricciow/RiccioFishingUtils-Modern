package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.data.mob.SkyblockEntity

object MobEvents {
    fun registerMobDetectEvent(
        priority: Int = 20,
        callback: (Set<SkyblockEntity>) -> Unit
    ): MobDetectEventManager.MobDetectEvent {
        return MobDetectEventManager.register(priority, callback)
    }

    fun registerMobDisposeEvent(
        priority: Int = 20,
        callback: (Set<SkyblockEntity>) -> Unit
    ): MobDisposeEventManager.MobDisposeEvent {
        return MobDisposeEventManager.register(priority, callback)
    }

    object MobDetectEventManager : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDetectEventManager.MobDetectEvent>() {
        override val runTasks: (Set<SkyblockEntity>) -> Unit = { mobs ->
            safeExecution {
                tasks.forEach { task -> task.callback(mobs) }
            }
        }

        fun register(priority: Int = 20, callback: (Set<SkyblockEntity>) -> Unit): MobDetectEvent {
            return MobDetectEvent(priority, callback).register()
        }

        class MobDetectEvent(
            priority: Int = 20,
            callback: (Set<SkyblockEntity>) -> Unit
        ) : ManagedTask<(Set<SkyblockEntity>) -> Unit, MobDetectEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object MobDisposeEventManager : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDisposeEventManager.MobDisposeEvent>() {
        override val runTasks: (Set<SkyblockEntity>) -> Unit = { mob ->
            safeExecution {
                tasks.forEach { task -> task.callback(mob) }
            }
        }

        fun register(priority: Int = 20, callback: (Set<SkyblockEntity>) -> Unit): MobDisposeEvent {
            return MobDisposeEvent(priority, callback).register()
        }

        class MobDisposeEvent(
            priority: Int = 20,
            callback: (Set<SkyblockEntity>) -> Unit
        ) : ManagedTask<(Set<SkyblockEntity>) -> Unit, MobDisposeEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
