package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.data.streak.DailyStreakData
import cloud.glitchdev.rfu.events.AbstractEventManager

object DailyStreakEvents : AbstractEventManager<(DailyStreakData) -> Unit, DailyStreakEvents.DailyStreakUpdatedEvent>() {
    override val runTasks: (DailyStreakData) -> Unit = { data ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(data)
            }
        }
    }

    fun registerStreakUpdatedEvent(priority: Int = 20, callback: (DailyStreakData) -> Unit): DailyStreakUpdatedEvent {
        return DailyStreakUpdatedEvent(priority, callback).register()
    }

    class DailyStreakUpdatedEvent(
        priority: Int = 20,
        callback: (DailyStreakData) -> Unit
    ) : ManagedTask<(DailyStreakData) -> Unit, DailyStreakUpdatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
