package cloud.glitchdev.rfu.config.migration

import cloud.glitchdev.rfu.utils.BackupManager
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

object SeaCreatureSettingsMigration {
    const val CURRENT_VERSION = 1

    fun runMigrations(configFile: Path): Boolean {
        if (!configFile.exists()) return false

        return try {
            val original = configFile.readText()
            val root = JsonParser.parseString(original).asJsonObject
            val version = root["version"]?.asInt ?: 0
            if (version >= CURRENT_VERSION) return false

            BackupManager.saveGzBackup("repo/sc-config.json.gz", original)

            for (currentVersion in version until CURRENT_VERSION) {
                when (currentVersion) {
                    0 -> migrateV0toV1(root)
                }
            }
            root.addProperty("version", CURRENT_VERSION)

            BackupManager.writeAtomically(configFile.toFile(), GsonBuilder().setPrettyPrinting().create().toJson(root))
            RFULogger.info("Migrated Sea Creature settings from version $version to $CURRENT_VERSION")
            true
        } catch (e: Exception) {
            RFULogger.error("Failed to migrate Sea Creature settings", e)
            false
        }
    }

    private fun migrateV0toV1(root: JsonObject) {
        val puddleJumper = root.getAsJsonObject("creatures")?.getAsJsonObject("Puddle Jumper") ?: return
        puddleJumper.addProperty("lsRangeEnabled", false)
        puddleJumper.addProperty("gdragAlert", false)
    }
}
