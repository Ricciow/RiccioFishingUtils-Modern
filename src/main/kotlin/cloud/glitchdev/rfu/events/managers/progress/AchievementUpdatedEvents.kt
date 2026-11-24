package cloud.glitchdev.rfu.events.managers.progress

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object AchievementUpdatedEvents : AbstractEventManager<(IAchievement) -> Unit, AchievementUpdatedEvents.AchievementUpdatedEvent>() {
    override val runTasks: (IAchievement) -> Unit = { achievement ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(achievement)
            }
        }
    }

    fun registerAchievementUpdatedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (IAchievement) -> Unit): AchievementUpdatedEvent {
        return AchievementUpdatedEvent(priority, callback).register()
    }

    class AchievementUpdatedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (IAchievement) -> Unit
    ) : ManagedTask<(IAchievement) -> Unit, AchievementUpdatedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
