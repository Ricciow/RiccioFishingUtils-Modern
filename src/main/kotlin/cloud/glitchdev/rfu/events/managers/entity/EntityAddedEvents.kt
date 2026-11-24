package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.world.entity.Entity

@EventManager(register = false)
object EntityAddedEvents : AbstractEventManager<(entity : Entity) -> Unit, EntityAddedEvents.EntityAddedEvent>() {
    override val runTasks: (Entity) -> Unit = { entity ->
        safeExecution {
            tasks.forEach { task -> task.callback(entity) }
        }
    }

    class EntityAddedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (entity : Entity) -> Unit
    ) : ManagedTask<(entity : Entity) -> Unit, EntityAddedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
