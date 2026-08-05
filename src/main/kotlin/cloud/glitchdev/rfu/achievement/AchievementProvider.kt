package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.events.managers.AchievementUnlockedEvents
import cloud.glitchdev.rfu.events.managers.AchievementStageUnlockedEvents
import cloud.glitchdev.rfu.events.managers.AchievementUpdatedEvents

object AchievementProvider {
    fun getVisibleAchievements(): List<IAchievement> {
        val all = AchievementManager.getRegistry().values
        return all.filter { 
            when (it.type) {
                AchievementType.NORMAL -> true
                AchievementType.SECRET -> true // Description masked by UI if not completed
                AchievementType.OBFUSCATED -> true // Obfuscated by UI if not completed
            }
        }
    }
    
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
