package cloud.glitchdev.rfu.events.managers.progress

import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.utils.RFULogger

@EventManager(register = false)
object AchievementStageUnlockedEvents : AbstractEventManager<(IStageAchievement) -> Unit, AchievementStageUnlockedEvents.AchievementStageUnlockedEvent>() {
    override val runTasks: (IStageAchievement) -> Unit = { achievement ->
        RFULogger.dev("Completed achievement stage ${achievement.currentStage-1} for ${achievement.name}")
        safeExecution {
            tasks.forEach { task ->
                task.callback(achievement)
            }
        }
    }

    fun registerAchievementStageUnlockedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (IStageAchievement) -> Unit): AchievementStageUnlockedEvent {
        return AchievementStageUnlockedEvent(priority, callback).register()
    }

    class AchievementStageUnlockedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (IStageAchievement) -> Unit
    ) : ManagedTask<(IStageAchievement) -> Unit, AchievementStageUnlockedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
