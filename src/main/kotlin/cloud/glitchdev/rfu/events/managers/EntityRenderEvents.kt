package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.wrappers.CancelableBoolean
import net.minecraft.world.entity.Entity

object EntityRenderEvents : AbstractEventManager<(entity : Entity, event : CancelableBoolean) -> Unit, EntityRenderEvents.EntityRenderEvent>(), RegisteredEvent {
    override fun register() {
        //Doesn't need registering because it's called from EntityRenderDispatcherMixin.java
    }

    fun runTasks(entity : Entity, event : CancelableBoolean) {
        tasks.forEach { task -> task.callback(entity, event) }
    }

    fun registerEntityRenderEvent(
        priority: Int = 20,
        callback: (entity : Entity, event : CancelableBoolean) -> Unit
    ): EntityRenderEvent {
        return EntityRenderEvent(priority, callback).register()
    }

    class EntityRenderEvent(
        priority: Int = 20,
        callback: (entity : Entity, event : CancelableBoolean) -> Unit
    ) : ManagedTask<(entity : Entity, event : CancelableBoolean) -> Unit, EntityRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}