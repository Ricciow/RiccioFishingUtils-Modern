package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.manager.mob.SkyblockEntity

object MobDetectEvents : AbstractEventManager<(Set<SkyblockEntity>) -> Unit, MobDetectEvents.MobDetectEvent>() {
    override val runTasks: (Set<SkyblockEntity>) -> Unit = { mobs ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(mobs)
            }
        }
    }

    fun registerMobDetectEvent(priority: Int = 20, callback: (Set<SkyblockEntity>) -> Unit): MobDetectEvent {
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