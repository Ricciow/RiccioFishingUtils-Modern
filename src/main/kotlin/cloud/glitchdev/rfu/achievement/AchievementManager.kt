package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.data.achievements.AchievementsData
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent

@AutoRegister
object AchievementManager : RegisteredEvent {
    private val registry = HashMap<String, BaseAchievement>()

    override fun register() {
        registerJoinEvent {
            saveAll()
        }

        registerShutdownEvent {
            saveAll()
        }
    }

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
        AchievementHandler.saveAll(allData)
    }
}
