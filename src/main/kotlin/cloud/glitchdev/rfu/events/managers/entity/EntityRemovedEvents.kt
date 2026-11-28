package cloud.glitchdev.rfu.events.managers.entity

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object EntityRemovedEvents : AbstractEventManager<(entityId : Int) -> Unit, EntityRemovedEvents.EntityRemovedEvent>() {
    override val runTasks: (Int) -> Unit = { entityId ->
        safeExecution {
            tasks.forEach { task -> task.callback(entityId) }
        }
    }

    class EntityRemovedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (entityId : Int) -> Unit
    ) : ManagedTask<(entityId : Int) -> Unit, EntityRemovedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}