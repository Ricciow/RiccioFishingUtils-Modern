package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable
import net.minecraft.world.entity.Entity

@EventManager(register = false)
object EntityRenderEvents : AbstractEventManager<(entity : Entity, isVisible: Boolean, event : Cancelable<Boolean>) -> Unit, EntityRenderEvents.EntityRenderEvent>() {
    override val runTasks: (Entity, Boolean, Cancelable<Boolean>) -> Unit = { entity, isVisible, event ->
        safeExecution {
            tasks.forEach { task -> task.callback(entity, isVisible, event) }
        }
    }

    class EntityRenderEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (entity : Entity, isVisible: Boolean, event : Cancelable<Boolean>) -> Unit
    ) : ManagedTask<(entity : Entity, isVisible: Boolean, event : Cancelable<Boolean>) -> Unit, EntityRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
