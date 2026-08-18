package cloud.glitchdev.rfu.achievement.migration

import cloud.glitchdev.rfu.utils.BackupManager
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object AchievementMigration {
    const val CURRENT_VERSION = 1
    const val VERSION_KEY = "version"

    fun runMigrations(achievementsFile: Path) {
        if (!achievementsFile.exists()) return

        val root = try {
            readJson(achievementsFile).asJsonObject
        } catch (e: Exception) {
            RFULogger.warn("[AchievementMigration] Failed to read achievements file for migration: ${e.message}")
            return
        }

        val currentVersion = root[VERSION_KEY]?.asInt ?: 0
        if (currentVersion >= CURRENT_VERSION) return

        try {
            BackupManager.saveGzBackup("data/achievements.json.gz", achievementsFile.readText())
        } catch (e: Exception) {
            RFULogger.warn("[AchievementMigration] Failed to save backup before achievements migration: ${e.message}")
        }

        RFULogger.info("[AchievementMigration] Migrating achievements data from version $currentVersion to $CURRENT_VERSION")

        processVersionChain(root, currentVersion)
        root.addProperty(VERSION_KEY, CURRENT_VERSION)

        try {
            writeJson(achievementsFile, root)
            RFULogger.info("[AchievementMigration] Successfully migrated achievements data to version $CURRENT_VERSION")
        } catch (e: Exception) {
            RFULogger.error("[AchievementMigration] Failed to write migrated achievements config: ${e.message}", e)
        }
    }

    private fun processVersionChain(json: JsonObject, from: Int) {
        for (version in from until CURRENT_VERSION) {
            when (version) {
                0 -> migrateV0toV1(json)
            }
        }
    }

    private fun migrateV0toV1(json: JsonObject) {
        if (!json.has("achievements")) {
            json.add("achievements", JsonObject())
        }
        if (!json.has("trackedAchievements")) {
            json.add("trackedAchievements", JsonArray())
        }

        val achievements = json.getAsJsonObject("achievements")
        if (achievements != null && achievements.has("survivalist")) {
            val survivalist = achievements.getAsJsonObject("survivalist")
            val isCompleted = survivalist.get("isCompleted")?.asBoolean ?: false
            val progressData = survivalist.getAsJsonObject("progressData")
            val currentStage = progressData?.get("currentStage")?.asInt ?: 1

            if (isCompleted && currentStage <= 5) {
                survivalist.addProperty("isCompleted", false)
                if (progressData != null && currentStage < 5) {
                    progressData.addProperty("currentStage", 5)
                }
            }
        }
    }

    fun renameAchievement(root: JsonObject, oldId: String, newId: String) {
        val achievements = root.getAsJsonObject("achievements")
        val trackedArray = root.getAsJsonArray("trackedAchievements")

        if (achievements != null && achievements.has(oldId)) {
            val achievementObj = achievements.getAsJsonObject(oldId)
            achievementObj.addProperty("id", newId)
            achievements.remove(oldId)
            achievements.add(newId, achievementObj)
        }

        if (trackedArray != null) {
            val newTracked = JsonArray()
            for (elem in trackedArray) {
                if (elem.asString == oldId) {
                    newTracked.add(newId)
                } else {
                    newTracked.add(elem)
                }
            }
            root.add("trackedAchievements", newTracked)
        }
    }

    fun removeAchievement(root: JsonObject, id: String) {
        val achievements = root.getAsJsonObject("achievements")
        achievements?.remove(id)

        val trackedArray = root.getAsJsonArray("trackedAchievements")
        if (trackedArray != null) {
            val newTracked = JsonArray()
            for (elem in trackedArray) {
                if (elem.asString != id) {
                    newTracked.add(elem)
                }
            }
            root.add("trackedAchievements", newTracked)
        }
    }

    fun transformProgressData(root: JsonObject, id: String, transform: (JsonObject) -> Unit) {
        val achievements = root.getAsJsonObject("achievements") ?: return
        val achievementObj = achievements.getAsJsonObject(id) ?: return
        val progressData = achievementObj.getAsJsonObject("progressData") ?: return
        transform(progressData)
    }

    private fun readJson(path: Path): JsonElement {
        return JsonParser.parseString(path.readText())
    }

    private fun writeJson(path: Path, json: JsonElement) {
        path.writeText(GsonBuilder().setPrettyPrinting().create().toJson(json))
    }
}
