package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

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
