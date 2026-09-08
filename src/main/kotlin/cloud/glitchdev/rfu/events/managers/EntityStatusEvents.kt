package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.world.entity.Entity

object EntityStatusEvents : AbstractEventManager<(entity: Entity, status: Byte) -> Unit, EntityStatusEvents.EntityStatusEvent>() {
    override val runTasks: (Entity, Byte) -> Unit = { entity, status ->
        safeExecution {
            tasks.forEach { task -> task.callback(entity, status) }
        }
    }

    fun registerEntityStatusEvent(
        priority: Int = 20,
        callback: (entity: Entity, status: Byte) -> Unit
    ): EntityStatusEvent {
        return EntityStatusEvent(priority, callback).register()
    }

    class EntityStatusEvent(
        priority: Int = 20,
        callback: (entity: Entity, status: Byte) -> Unit
    ) : ManagedTask<(entity: Entity, status: Byte) -> Unit, EntityStatusEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
