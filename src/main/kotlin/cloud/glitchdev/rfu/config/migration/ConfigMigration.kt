package cloud.glitchdev.rfu.config.migration

import cloud.glitchdev.rfu.utils.BackupManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.roundToInt

object ConfigMigration {
    const val CURRENT_VERSION = 11
    const val VERSION_KEY = "rfuConfigVersion"

    private val logger = LoggerFactory.getLogger(ConfigMigration::class.java)

    fun runMigrations(configFile: Path) {
        if (!configFile.exists()) return

        val root = try {
            readJsonc(configFile).asJsonObject
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to read config for migration: ${e.message}")
            return
        }

        val currentVersion = root[VERSION_KEY]?.asInt ?: 0
        if (currentVersion >= CURRENT_VERSION) return

        try {
            BackupManager.saveGzBackup("settings.jsonc.gz", configFile.readText())
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to save backup before config migration: ${e.message}")
        }

        processVersionChain(root, currentVersion)
        root.addProperty(VERSION_KEY, CURRENT_VERSION)

        try {
            writeJson(configFile, root)
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to write migrated config: ${e.message}")
        }
    }

    private fun processVersionChain(json: JsonObject, from: Int) {
        for (version in from until CURRENT_VERSION) {
            when (version) {
                0 -> migrateV0toV1(json)
                1 -> migrateV1toV2(json)
                2 -> migrateV2toV3(json)
                3 -> migrateV3toV4(json)
                4 -> migrateV4toV5(json)
                5 -> migrateV5toV6(json)
                6 -> migrateV6toV7(json)
                7 -> migrateV7toV8(json)
                8 -> migrateV8toV9(json)
                9 -> migrateV9toV10(json)
                10 -> migrateV10toV11(json)
            }
        }
    }

    private fun migrateV0toV1(json: JsonObject) {
        val flareTimer = deleteKey(json, "General Fishing", "flareTimerDisplay")
        val umberellaTimer = deleteKey(json, "General Fishing", "umberellaTimerDisplay")
        val flareAlert = deleteKey(json, "General Fishing", "flareAlert")
        val umberellaAlert = deleteKey(json, "General Fishing", "umberellaAlert")

        val cat = getCategory(json, "General Fishing") ?: return

        if (!cat.has("deployableTimerDisplay")) {
            val arr = JsonArray()
            if (flareTimer?.asBoolean == true) arr.add("FLARE")
            if (umberellaTimer?.asBoolean == true) arr.add("UMBERELLA")
            cat.add("deployableTimerDisplay", arr)
        }

        if (!cat.has("deployableAlertTypes")) {
            val arr = JsonArray()
            if (flareAlert?.asBoolean == true) arr.add("FLARE")
            if (umberellaAlert?.asBoolean == true) arr.add("UMBERELLA")
            cat.add("deployableAlertTypes", arr)
        }

        val newCat = getOrCreateCategory(json, "Rare SCs")
        val rareScKeys = listOf(
            "rareSC", "lootshareRange", "detectionAlert", "timeToKill",
            "rarePartyMessages", "rarePartyMessage", "dhText",
            "bossHealthBars", "healthBarMobs", "boostPollingRate", "coloredShurikenBar"
        )
        rareScKeys.forEach { key ->
            val value = deleteKey(json, "General Fishing", key)
            if (value != null && !newCat.has(key)) newCat.add(key, value)
        }
    }

    private fun migrateV1toV2(json: JsonObject) {
        val schDisplay = deleteKey(json, "General Fishing", "schDisplay")?.asBoolean ?: true
        val schTimer = deleteKey(json, "General Fishing", "schTimer")?.asBoolean ?: true
        val schOverall = deleteKey(json, "General Fishing", "schOverall")?.asBoolean ?: false
        val schOnlyWhenFishing = deleteKey(json, "General Fishing", "schOnlyWhenFishing")?.asBoolean ?: true

        val xphDisplay = deleteKey(json, "General Fishing", "xphDisplay")?.asBoolean ?: true
        val xphTimer = deleteKey(json, "General Fishing", "xphTimer")?.asBoolean ?: false
        val xphOverall = deleteKey(json, "General Fishing", "xphOverall")?.asBoolean ?: false
        val xphOnlyWhenFishing = deleteKey(json, "General Fishing", "xphOnlyWhenFishing")?.asBoolean ?: true

        val cat = getCategory(json, "General Fishing") ?: return

        if (!cat.has("fishTrackingDisplay")) {
            cat.addProperty("fishTrackingDisplay", schDisplay || xphDisplay)
        }

        if (!cat.has("fishTrackingItems")) {
            val arr = JsonArray()
            if (schDisplay) arr.add("SC_H")
            if (xphDisplay) arr.add("XP_H")
            if (schTimer || xphTimer) arr.add("TIMER")
            if (schOverall || xphOverall) arr.add("OVERALL")
            cat.add("fishTrackingItems", arr)
        }

        if (!cat.has("fishTrackingOnlyWhenFishing")) {
            cat.addProperty("fishTrackingOnlyWhenFishing", schOnlyWhenFishing && xphOnlyWhenFishing)
        }
    }

    private fun migrateV2toV3(json: JsonObject) {
        val newCat = getOrCreateCategory(json, "Drops")
        val rareDropKeys = listOf(
            "rareDrops", "dyeDrops", "customRareDropMessage", "rareDropMessageFormat",
            "rareDropPartyChat", "lootshareMessage", "rareDropTitleAlert",
            "rareDropTitleFormat", "rareDropSubtitleFormat"
        )
        rareDropKeys.forEach { key ->
            val value = deleteKey(json, "General Fishing", key)
            if (value != null && !newCat.has(key)) newCat.add(key, value)
        }
    }

    private fun migrateV3toV4(json: JsonObject) {
        val oldScCat = deleteCategory(json, "Rare SCs")
        val newScCat = getOrCreateCategory(json, "Sea Creatures")
        if (oldScCat != null) {
            oldScCat.entrySet().forEach { (key, value) ->
                if (!newScCat.has(key)) newScCat.add(key, value)
            }
        }

        val catchMsgKeys = listOf("replaceCatchMessages", "catchMessageTemplate", "doubleHookCatchMessageTemplate")
        catchMsgKeys.forEach { key ->
            val value = deleteKey(json, "General Fishing", key)
            if (value != null && !newScCat.has(key)) newScCat.add(key, value)
        }
    }

    private fun migrateV4toV5(json: JsonObject) {
        val cat = getCategory(json, "Ink Fishing") ?: return
        val items = cat["inkTrackingItems"]?.asJsonArray ?: return
        
        var hasOverall = false
        for (item in items) {
            if (item.asString == "OVERALL") {
                hasOverall = true
                break
            }
        }
        
        if (!hasOverall) {
            items.add("OVERALL")
        }
    }

    private fun migrateV5toV6(json: JsonObject) {
        val cat = getCategory(json, "General Fishing") ?: return
        val value = cat["fishingTime"]?.asInt ?: return

        if(value < 5) {
            deleteKey(json, "General Fishing", "fishingTime")
            cat.addProperty("fishingTime", 5)
        }

    }

    private fun migrateV6toV7(json: JsonObject) {
        val cat = getCategory(json, "Drops") ?: return
        val items = cat["rareDrops"]?.asJsonArray ?: return

        val newDrops = listOf("SNAKE_EYES", "DEEP_SEA_ORB", "PYROCLASM_BOOK", "PRINCE_CROWN_JEWEL", "SCUTTLER_SHELL")
        for (drop in newDrops) {
            var exists = false
            for (item in items) {
                if (item.asString == drop) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                items.add(drop)
            }
        }
    }

    private fun migrateV7toV8(json: JsonObject) {
        val cat = getOrCreateCategory(json, "Other")
        cat.addProperty("autoAcceptResourcePacks", false)
    }

    private fun migrateV8toV9(json: JsonObject) {
        val cat = getOrCreateCategory(json, "Backend Settings")
        cat.addProperty("backendAccepted", false)
        cat.addProperty("decisionMade", false)
    }

    private fun migrateV9toV10(json: JsonObject) {
        val volumeSettings = mapOf(
            "Daily Streaks" to listOf("completionVolume"),
            "General Fishing" to listOf("deployableExpiredVolume", "failCastVolume"),
            "Hot Spots" to listOf("hotspotExpiredVolume"),
            "Ink Fishing" to listOf("rainAlertVolume"),
            "Lava Fishing" to listOf("diedJawbusVolume"),
            "Other" to listOf("achievementVolume", "littlefootVolume", "fullInventoryVolume"),
            "Sea Creatures" to listOf("rareScSoundVolume", "goldenDragonVolume")
        )
        for ((category, keys) in volumeSettings) {
            val cat = getCategory(json, category) ?: continue
            for (key in keys) {
                val value = cat[key] ?: continue
                cat.addProperty(key, (value.asDouble.coerceIn(0.0, 5.0) * 100).roundToInt())
            }
        }
    }

    private fun migrateV10toV11(json: JsonObject) {
        fun migrate(categoryName: String, editor: String, durationKey: String, baseTicks: Int, vararg fields: Pair<String, String>) {
            val category = getCategory(json, categoryName) ?: return
            val destination = getOrCreateCategory(category, editor)
            for ((oldKey, newKey) in fields) {
                val value = category.remove(oldKey) ?: continue
                if (!destination.has(newKey)) destination.add(newKey, value)
            }
            val extraTicks = category.remove(durationKey)?.asInt ?: 0
            if (!destination.has("durationSeconds")) {
                destination.addProperty("durationSeconds", (baseTicks + extraTicks).coerceIn(0, 600) / 20.0)
            }
        }

        migrate("General Fishing", "failCast", "failCastTitleExtraTicks", 5,
            "failCastAlert" to "enabled", "failCastSound" to "sound", "failCastVolume" to "volume"
        )
        migrate("General Fishing", "noBait", "noBaitTitleExtraTicks", 10,
            "noBaitAlert" to "enabled"
        )
        migrate("General Fishing", "missingRodPiece", "missingRodPieceTitleExtraTicks", 10,
            "missingRodPieceAlert" to "enabled"
        )
        migrate("General Fishing", "noFishingArmor", "noFishingArmorTitleExtraTicks", 10,
            "noFishingArmorAlert" to "enabled"
        )
        migrate("General Fishing", "deployable", "deployableTitleExtraTicks", 10,
            "deployableExpiredAlert" to "enabled", "deployableExpiredSound" to "sound", "deployableExpiredVolume" to "volume", "deployableAlertTypes" to "types"
        )
        migrate("Hot Spots", "noTikiMask", "noTikiMaskTitleExtraTicks", 10,
            "noTikiMaskAlert" to "enabled"
        )
        migrate("Hot Spots", "hotspotExpired", "hotspotExpiredTitleExtraTicks", 10,
            "hotspotExpiredAlert" to "enabled", "hotspotExpiredSound" to "sound", "hotspotExpiredVolume" to "volume"
        )
        migrate("Ink Fishing", "rain", "rainTitleExtraTicks", 20,
            "rainAlert" to "enabled", "rainAlertSound" to "sound", "rainAlertVolume" to "volume"
        )
        migrate("Jerry Fishing", "reindrake", "reindrakeTitleExtraTicks", 20,
            "reindrakeAlert" to "enabled"
        )
        migrate("Sea Creatures", "rareSc", "rareScTitleExtraTicks", 10,
            "detectionAlert" to "enabled", "rareScSound" to "sound", "rareScSoundVolume" to "volume", "rareScAlertPreset" to "titleTemplate"
        )
        migrate("Sea Creatures", "goldenDragon", "goldenDragonTitleExtraTicks", 40,
            "goldenDragonAlert" to "enabled", "goldenDragonSound" to "sound", "goldenDragonVolume" to "volume", "gdragAlertThreshold" to "threshold"
        )
        migrate("Lava Fishing", "jawbusDeath", "jawbusDeathTitleExtraTicks", 10,
            "diedJawbusAlert" to "enabled", "diedJawbusSound" to "sound", "diedJawbusVolume" to "volume"
        )
        migrate("Other", "littlefoot", "littlefootTitleExtraTicks", 10,
            "littlefootAlert" to "enabled", "littlefootSound" to "sound", "littlefootVolume" to "volume"
        )
        migrate("Drops", "rareDrop", "rareDropTitleExtraTicks", 40,
            "rareDropTitleAlert" to "enabled", "rareDropTitleFormat" to "titleTemplate", "rareDropSubtitleFormat" to "subtitleTemplate"
        )
        migrate("Other", "petLevelUp", "petLevelUpTitleExtraTicks", 40,
            "petLevelUpAlert" to "enabled", "petLevelUpMinLevel" to "minimumLevel"
        )
        migrate("Other", "fullInventory", "fullInventoryTitleExtraTicks", 20,
            "fullInventoryAlert" to "enabled", "fullInventorySound" to "sound", "fullInventoryVolume" to "volume"
        )
    }

    private fun deleteKey(json: JsonObject, category: String, key: String): JsonElement? {
        val cat = json[category]?.asJsonObject ?: return null
        val value = cat[key]
        cat.remove(key)
        return value
    }

    private fun deleteCategory(json: JsonObject, category: String): JsonObject? {
        val cat = json[category]?.asJsonObject ?: return null
        json.remove(category)
        return cat
    }

    private fun getCategory(json: JsonObject, name: String): JsonObject? {
        return json[name]?.asJsonObject
    }

    private fun getOrCreateCategory(json: JsonObject, name: String): JsonObject {
        return json[name]?.asJsonObject ?: JsonObject().also { json.add(name, it) }
    }

    private fun stripJsoncComments(text: String): String {
        val sb = StringBuilder(text.length)
        var i = 0
        var inString = false
        while (i < text.length) {
            val c = text[i]
            if (inString) {
                sb.append(c)
                if (c == '\\' && i + 1 < text.length) {
                    sb.append(text[++i])
                } else if (c == '"') {
                    inString = false
                }
            } else {
                when {
                    c == '"' -> {
                        inString = true
                        sb.append(c)
                    }
                    c == '/' && i + 1 < text.length && text[i + 1] == '/' -> {
                        while (i < text.length && text[i] != '\n') i++
                        continue
                    }
                    c == '/' && i + 1 < text.length && text[i + 1] == '*' -> {
                        i += 2
                        while (i + 1 < text.length && !(text[i] == '*' && text[i + 1] == '/')) i++
                        i += 2
                        continue
                    }
                    else -> sb.append(c)
                }
            }
            i++
        }
        return sb.toString()
    }

    private fun readJsonc(path: Path): JsonElement {
        val stripped = stripJsoncComments(path.readText())
        return JsonParser.parseString(stripped)
    }

    private fun writeJson(path: Path, json: JsonElement) {
        path.writeText(GsonBuilder().setPrettyPrinting().create().toJson(json))
    }
}
