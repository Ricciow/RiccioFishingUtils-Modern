package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.world.entity.Entity

@EventManager(register = false)
object EntityDataEvents : AbstractEventManager<(entity : Entity) -> Unit, EntityDataEvents.EntityDataEvent>() {
    override val runTasks: (Entity) -> Unit = { entity ->
        safeExecution {
            tasks.forEach { task -> task.callback(entity) }
        }
    }

    fun registerEntityDataEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (entity : Entity) -> Unit
    ): EntityDataEvent {
        return EntityDataEvent(priority, callback).register()
    }

    class EntityDataEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (entity : Entity) -> Unit
    ) : ManagedTask<(entity : Entity) -> Unit, EntityDataEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
