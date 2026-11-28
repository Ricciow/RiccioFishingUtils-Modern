package cloud.glitchdev.rfu.events.managers.loot

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.model.dye.Dyes

@EventManager(register = false)
object DyeEvents : AbstractEventManager<(dyes: Dyes?) -> Unit, DyeEvents.DyeUpdateEvent>() {
    override val runTasks: (Dyes?) -> Unit = { dyes ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(dyes)
            }
        }
    }

    class DyeUpdateEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Dyes?) -> Unit
    ) : ManagedTask<(Dyes?) -> Unit, DyeUpdateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
