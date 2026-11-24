package cloud.glitchdev.rfu.events.managers.progress

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.utils.RFULogger

@EventManager(register = false)
object AchievementUnlockedEvents : AbstractEventManager<(IAchievement) -> Unit, AchievementUnlockedEvents.AchievementUnlockedEvent>() {
    override val runTasks: (IAchievement) -> Unit = { achievement ->
        RFULogger.dev("Completed achievement ${achievement.name}")
        safeExecution {
            tasks.forEach { task ->
                task.callback(achievement)
            }
        }
    }

    fun registerAchievementUnlockedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (IAchievement) -> Unit): AchievementUnlockedEvent {
        return AchievementUnlockedEvent(priority, callback).register()
    }

    class AchievementUnlockedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (IAchievement) -> Unit
    ) : ManagedTask<(IAchievement) -> Unit, AchievementUnlockedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
