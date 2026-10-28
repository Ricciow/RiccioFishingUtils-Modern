package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.world.entity.Entity

object EntityFireRenderEvents : AbstractEventManager<(EntityFireRenderEvents.EntityFireContext) -> Unit, EntityFireRenderEvents.EntityFireEvent>() {
    override val runTasks: (EntityFireContext) -> Unit = { context ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(context) }
        }
    }

    fun registerEntityFireRenderEvent(priority: Int = 20, callback: (EntityFireContext) -> Unit): EntityFireEvent =
        EntityFireEvent(priority, callback).register()

    class EntityFireContext(val entity: Entity, var visible: Boolean)

    class EntityFireEvent(priority: Int, callback: (EntityFireContext) -> Unit) :
        ManagedTask<(EntityFireContext) -> Unit, EntityFireEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}