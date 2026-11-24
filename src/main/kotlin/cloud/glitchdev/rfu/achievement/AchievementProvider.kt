package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.events.managers.progress.AchievementUnlockedEvents
import cloud.glitchdev.rfu.events.managers.progress.AchievementStageUnlockedEvents
import cloud.glitchdev.rfu.events.managers.progress.AchievementUpdatedEvents

object AchievementProvider {
    fun isVisible(achievement: IAchievement): Boolean =
        achievement.category != AchievementCategory.LEGACY || achievement.isCompleted

    fun getVisibleAchievements(): List<IAchievement> =
        AchievementManager.getRegistry().values.filter(::isVisible)
    
    fun fireAchievementUnlocked(achievement: IAchievement) {
        AchievementUnlockedEvents.runTasks(achievement)
    }

    fun fireAchievementStageUnlocked(achievement: IStageAchievement) {
        AchievementStageUnlockedEvents.runTasks(achievement)
    }

    fun fireAchievementUpdated(achievement: IAchievement) {
        AchievementUpdatedEvents.runTasks(achievement)
    }
}
