package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

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
