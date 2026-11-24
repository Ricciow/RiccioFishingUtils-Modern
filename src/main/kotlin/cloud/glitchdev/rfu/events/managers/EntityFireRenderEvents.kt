package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.world.entity.Entity

@EventManager(register = false, name = "registerEntityFireRenderEvent")
object EntityFireRenderEvents : AbstractEventManager<(EntityFireRenderEvents.EntityFireContext) -> Unit, EntityFireRenderEvents.EntityFireEvent>() {
    override val runTasks: (EntityFireContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    fun registerEntityFireRenderEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (EntityFireContext) -> Unit): EntityFireEvent =
        EntityFireEvent(priority, callback).register()

    class EntityFireContext(val entity: Entity, var visible: Boolean)

    class EntityFireEvent(priority: Int, callback: (EntityFireContext) -> Unit) :
        ManagedTask<(EntityFireContext) -> Unit, EntityFireEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}