package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

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
