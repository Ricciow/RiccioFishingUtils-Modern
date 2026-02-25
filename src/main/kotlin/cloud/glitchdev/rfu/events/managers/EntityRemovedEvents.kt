package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.RegisteredEvent

object EntityRemovedEvents : AbstractEventManager<(entityId : Int) -> Unit, EntityRemovedEvents.EntityRemovedEvent>(), RegisteredEvent {
    override fun register() {
        //Doesn't need registering because it's called from ClientPacketListenerMixin.java
    }

    fun runTasks(entityId : Int) {
        tasks.forEach { task -> task.callback(entityId) }
    }

    fun registerEntityRemovedEvent(
        priority: Int = 20,
        callback: (entityId : Int) -> Unit
    ): EntityRemovedEvent {
        return EntityRemovedEvent(priority, callback).register()
    }

    class EntityRemovedEvent(
        priority: Int = 20,
        callback: (entityId : Int) -> Unit
    ) : ManagedTask<(entityId : Int) -> Unit, EntityRemovedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}