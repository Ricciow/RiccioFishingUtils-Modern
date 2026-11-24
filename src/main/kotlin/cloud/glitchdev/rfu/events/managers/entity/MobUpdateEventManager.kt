package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

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
