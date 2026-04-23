package cloud.glitchdev.rfu.data.achievements

import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.utils.JsonFile

object AchievementHandler {
    private val jsonFile = JsonFile(
        filename = "achievements.json",
        type = AchievementsData::class.java,
        defaultFactory = { AchievementsData() },
        onSave = { AchievementManager.saveAll() }
    )
    
    fun getAchievementData(id: String): AchievementsData.AchievementSaveData? {
        return jsonFile.data.achievements[id]
    }

    fun isTracked(id: String): Boolean {
        return jsonFile.data.trackedAchievements.contains(id)
    }

    fun setTracked(id: String, tracked: Boolean) {
        if (tracked) {
            jsonFile.data.trackedAchievements.add(id)
        } else {
            jsonFile.data.trackedAchievements.remove(id)
        }
        jsonFile.save()
    }

    fun getTrackedAchievements(): Set<String> {
        return jsonFile.data.trackedAchievements
    }
    
    fun updateData(data: Map<String, AchievementsData.AchievementSaveData>) {
        jsonFile.data.achievements.putAll(data)
    }
}
