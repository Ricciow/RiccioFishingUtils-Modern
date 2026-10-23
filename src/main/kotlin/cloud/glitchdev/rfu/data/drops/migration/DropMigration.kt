package cloud.glitchdev.rfu.data.drops.migration

import cloud.glitchdev.rfu.constants.fishing.IRareDrop
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import cloud.glitchdev.rfu.utils.BackupManager
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object DropMigration {
    const val CURRENT_VERSION = 1
    const val VERSION_KEY = "version"

    fun runMigrations(root: JsonObject) {
        val currentVersion = root[VERSION_KEY]?.asInt ?: 0
        if (currentVersion >= CURRENT_VERSION) return

        try {
            val original = GsonBuilder().setPrettyPrinting().create().toJson(root)
            BackupManager.saveGzBackup("data/drops.json.gz", original)
        } catch (e: Exception) {
            RFULogger.warn("[DropMigration] Failed to save backup before drops migration: ${e.message}")
        }

        RFULogger.info("[DropMigration] Migrating drops data from version $currentVersion to $CURRENT_VERSION")
        processVersionChain(root, currentVersion)
        root.addProperty(VERSION_KEY, CURRENT_VERSION)
        RFULogger.info("[DropMigration] Successfully migrated drops data to version $CURRENT_VERSION")
    }

    private fun processVersionChain(json: JsonObject, from: Int) {
        for (version in from until CURRENT_VERSION) {
            when (version) {
                0 -> migrateV0toV1(json)
            }
        }
    }

    private fun migrateV0toV1(json: JsonObject) {
        migrateEntries(json, "drops", RareDrops.entries.map { it.name to it })
        migrateEntries(json, "dyeDrops", Dyes.entries.map { it.name to it })
    }

    private fun migrateEntries(root: JsonObject, key: String, drops: List<Pair<String, IRareDrop>>) {
        val entries = root[key]?.takeIf { it.isJsonArray }?.asJsonArray
            ?: JsonArray().also { root.add(key, it) }

        drops.forEach { (type, drop) ->
            val entry = entries.firstOrNull {
                it.isJsonObject && it.asJsonObject["type"]?.asString == type
            }?.asJsonObject ?: JsonObject().also {
                it.addProperty("type", type)
                it.add("history", JsonArray())
                entries.add(it)
            }
            val history = entry["history"]?.takeIf { it.isJsonArray }?.asJsonArray
            if (entry["currentCount"] == null || entry["currentCount"].isJsonNull) {
                val total = drop.relatedScs.sumOf { catchHistory.getOrAdd(it).total }
                val lastTotal = history?.lastOrNull { it.isJsonObject }?.asJsonObject
                    ?.get("totalCount")?.takeUnless { it.isJsonNull }?.asInt ?: 0
                entry.addProperty("currentCount", (total - lastTotal).coerceAtLeast(0))
            }
            history?.filter { it.isJsonObject }?.forEach { it.asJsonObject.remove("totalCount") }
        }
    }
}
