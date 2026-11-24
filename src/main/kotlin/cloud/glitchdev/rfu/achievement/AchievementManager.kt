package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.data.achievements.AchievementsData

object AchievementManager {
    private val registry = HashMap<String, BaseAchievement>()

    fun register(achievement: BaseAchievement) {
        registry[achievement.id] = achievement
    }
    
    fun getAchievement(id: String): BaseAchievement? = registry[id]

    fun getRegistry(): Map<String, BaseAchievement> = registry

    fun saveAll() {
        val allData = registry.mapValues { (_, achievement) ->
            AchievementsData.AchievementSaveData(
                id = achievement.id,
                isCompleted = achievement.isCompleted,
                isCheated = achievement.isCheated,
                progressData = achievement.saveState()
            )
        }
        AchievementHandler.updateData(allData)
    }

    fun reloadAll() {
        registry.values.forEach {
            it.reloadState()
        }
        registry.values.forEach {
            it.postReload()
        }
    }
}
