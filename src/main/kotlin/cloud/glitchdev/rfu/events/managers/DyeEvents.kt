package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.model.dye.Dyes

object DyeEvents : AbstractEventManager<(dyes: Dyes?) -> Unit, DyeEvents.DyeUpdateEvent>() {
    override val runTasks: (Dyes?) -> Unit = { dyes ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(dyes)
            }
        }
    }

    fun registerDyeUpdateEvent(priority: Int = 20, callback: (Dyes?) -> Unit): DyeUpdateEvent {
        return DyeUpdateEvent(priority, callback).register()
    }

    class DyeUpdateEvent(
        priority: Int = 20,
        callback: (Dyes?) -> Unit
    ) : ManagedTask<(Dyes?) -> Unit, DyeUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }

    fun trigger(dyes: Dyes?) {
        runTasks(dyes)
    }
}
