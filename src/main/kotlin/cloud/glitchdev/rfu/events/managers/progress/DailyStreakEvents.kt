package cloud.glitchdev.rfu.events.managers.progress

import cloud.glitchdev.rfu.data.streak.DailyStreakData
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false, name = "registerStreakUpdatedEvent")
object DailyStreakEvents : AbstractEventManager<(DailyStreakData) -> Unit, DailyStreakEvents.DailyStreakUpdatedEvent>() {
    override val runTasks: (DailyStreakData) -> Unit = { data ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(data)
            }
        }
    }

    fun registerStreakUpdatedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (DailyStreakData) -> Unit): DailyStreakUpdatedEvent {
        return DailyStreakUpdatedEvent(priority, callback).register()
    }

    class DailyStreakUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (DailyStreakData) -> Unit
    ) : ManagedTask<(DailyStreakData) -> Unit, DailyStreakUpdatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
