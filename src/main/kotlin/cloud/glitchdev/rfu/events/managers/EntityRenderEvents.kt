package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable
import net.minecraft.world.entity.Entity

object EntityRenderEvents : AbstractEventManager<(entity : Entity, event : Cancelable<Boolean>) -> Unit, EntityRenderEvents.EntityRenderEvent>() {
    override val runTasks: (Entity, Cancelable<Boolean>) -> Unit = { entity, event ->
        safeExecution {
            tasks.forEach { task -> task.callback(entity, event) }
        }
    }

    fun registerEntityRenderEvent(
        priority: Int = 20,
        callback: (entity : Entity, event : Cancelable<Boolean>) -> Unit
    ): EntityRenderEvent {
        return EntityRenderEvent(priority, callback).register()
    }

    class EntityRenderEvent(
        priority: Int = 20,
        callback: (entity : Entity, event : Cancelable<Boolean>) -> Unit
    ) : ManagedTask<(entity : Entity, event : Cancelable<Boolean>) -> Unit, EntityRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}