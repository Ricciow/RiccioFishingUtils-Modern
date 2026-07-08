package cloud.glitchdev.rfu.config.seacreatures

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.InstantRegister
import cloud.glitchdev.rfu.events.InstantRegisteredEvent
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.SeaCreatureCategory
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.utils.World
import net.minecraft.world.phys.Vec3
import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.network.Network
import cloud.glitchdev.rfu.config.categories.BackendSettings
import com.google.gson.Gson
import java.lang.reflect.Field

@InstantRegister
@AutoRegister
object SeaCreatureSettingsManager : InstantRegisteredEvent, RegisteredEvent {
    val seaCreatureConfigFile = JsonFile(
        directory = "repo",
        filename = "sc-config.json",
        type = SeaCreatureSettings::class.java,
        defaultFactory = { loadDefaults() }
    )

    private val defaultConfig: SeaCreatureSettings by lazy { loadDefaults() }
    private val scConfig get() = seaCreatureConfigFile.data

    private fun loadDefaults(): SeaCreatureSettings {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("assets/rfu/defaults/sc-config.json")
            ?: return SeaCreatureSettings.empty()

        return try {
            Gson().fromJson(stream.bufferedReader(), SeaCreatureSettings::class.java)
        } catch (e: Exception) {
            RFULogger.error("Failed to load Sea Creature defaults", e)
            SeaCreatureSettings.empty()
        }
    }

    fun getName(scName: String): String = resolve(scName) { it.name } ?: scName
    fun getPlural(scName: String): String = resolve(scName) { it.plural } ?: scName
    fun getArticle(scName: String): String = resolve(scName) { it.article } ?: ""
    fun getStyle(scName: String): String = resolve(scName) { it.style } ?: ""
    fun isSpecial(scName: String): Boolean = resolve(scName) { it.special } ?: false
    fun isLsRangeEnabled(scName: String): Boolean = isSpecial(scName) && (resolve(scName) { it.lsRangeEnabled } ?: false)
    fun isGdragAlert(scName: String): Boolean = isSpecial(scName) && (resolve(scName) { it.gdragAlert } ?: false)
    fun isRareSCAlert(scName: String): Boolean = isSpecial(scName) && (resolve(scName) { it.rareSCAlert } ?: false)
    fun isBossbarEnabled(scName: String): Boolean = isSpecial(scName) && (resolve(scName) { it.bossbar } ?: false)
    fun getScDisplayColor(scName: String): String = resolve(scName) { it.scDisplayColor } ?: "§f"

    fun save() {
        seaCreatureConfigFile.save()
    }

    fun updateCreature(scName: String, update: (SeaCreatureSetting) -> SeaCreatureSetting): Boolean {
        val current = scConfig.creatures[scName] ?: defaultConfig.creatures[scName] ?: run {
            RFULogger.error("Attempted to update unknown Sea Creature: $scName")
            return false
        }
        var updated = update(current)
        
        if (updated.special == false) {
            updated = updated.copy(
                lsRangeEnabled = false,
                bossbar = false,
                gdragAlert = false,
                rareSCAlert = false
            )
        }

        if (updated == current) return false

        RFULogger.info("Updating local Sea Creature setting: $scName")
        val creatures = scConfig.creatures.toMutableMap()
        creatures[scName] = updated

        val newSettings = scConfig.copy(creatures = creatures)

        try {
            val field: Field = JsonFile::class.java.getDeclaredField("data")
            field.isAccessible = true
            field.set(seaCreatureConfigFile, newSettings)
        } catch (e: Exception) {
            RFULogger.error("Failed to update Sea Creature $scName in config file", e)
            return false
        }

        registerCreature(scName)
        return true
    }

    private fun registerCreature(scName: String) {
        try {
            val liquidType = try {
                val raw = resolve(scName) { it.liquidType }
                if (raw != null) LiquidTypes.valueOf(raw) else LiquidTypes.WATER
            } catch (e: Exception) {
                RFULogger.error("Invalid liquid type for $scName: ${resolve(scName) { it.liquidType }}. Skipping registration.")
                return
            }

            val category = try {
                val raw = resolve(scName) { it.category }
                if (raw != null) SeaCreatureCategory.valueOf(raw) else null
            } catch (e: Exception) {
                RFULogger.error("Invalid category for $scName: ${resolve(scName) { it.category }}. Skipping registration.")
                return
            }

            if (category == null) {
                RFULogger.error("Category is missing for $scName. Skipping registration.")
                return
            }

            val sc = SeaCreatures(
                scName = scName,
                scDisplayName = resolve(scName) { it.name } ?: scName,
                article = resolve(scName) { it.article } ?: "",
                plural = resolve(scName) { it.plural } ?: scName,
                style = resolve(scName) { it.style } ?: "",
                special = resolve(scName) { it.special } ?: false,
                catchMessage = resolve(scName) { it.catchMessage } ?: "",
                liquidType = liquidType,
                category = category,
                condition = buildCondition(scConfig.creatures[scName]?.conditions ?: defaultConfig.creatures[scName]?.conditions),
                lsRangeEnabled = isLsRangeEnabled(scName),
                bossbar = isBossbarEnabled(scName),
                gdragAlert = isGdragAlert(scName),
                rareSCAlert = isRareSCAlert(scName),
                scDisplayColor = resolve(scName) { it.scDisplayColor } ?: "§f",
                rarePartyMessage = resolve(scName) { it.rarePartyMessage } ?: ""
            )
            SeaCreatures.register(sc)
            RFULogger.dev("Registered Sea Creature: $scName")
        } catch (e: Exception) {
            RFULogger.error("Critical error registering Sea Creature $scName", e)
        }
    }

    private fun <T> resolve(
        scName: String,
        extractor: (SeaCreatureSetting) -> T?
    ): T? {
        val configured = scConfig.creatures[scName]?.let { extractor(it) }
        if (configured != null) return configured
        return defaultConfig.creatures[scName]?.let { extractor(it) }
    }

    fun updateFromBackend() {
        Network.getRequest("${RiccioFishingUtils.API_URL}/config/sc-config") { response ->
            if (response.isSuccessful()) {
                val gson = Gson()
                val backendConfig = try {
                    gson.fromJson(response.body, SeaCreatureSettings::class.java)
                } catch (e: Exception) {
                    RFULogger.error("Failed to parse backend Sea Creature settings", e)
                    null
                } ?: return@getRequest

                val currentConfig = seaCreatureConfigFile.data
                val mergedCreatures = currentConfig.creatures.toMutableMap()
                var updatedCount = 0
                var addedCount = 0
                var deletedCount = 0

                backendConfig.creatures.forEach { (scName, backendSc) ->
                    try {
                        val currentSc = mergedCreatures[scName]
                        if (currentSc == null) {
                            mergedCreatures[scName] = backendSc
                            addedCount++
                            RFULogger.dev("New Sea Creature from backend: $scName")
                        } else {
                            val updatedSc = currentSc.copy(
                                name = currentSc.name ?: backendSc.name,
                                plural = currentSc.plural ?: backendSc.plural,
                                article = currentSc.article ?: backendSc.article,
                                style = currentSc.style ?: backendSc.style,
                                scDisplayColor = currentSc.scDisplayColor ?: backendSc.scDisplayColor,
                                rarePartyMessage = currentSc.rarePartyMessage ?: backendSc.rarePartyMessage,
                                catchMessage = backendSc.catchMessage,
                                liquidType = backendSc.liquidType,
                                category = backendSc.category,
                                conditions = backendSc.conditions
                            )
                            if (updatedSc != currentSc) {
                                mergedCreatures[scName] = updatedSc
                                updatedCount++
                                RFULogger.dev("Updated Sea Creature from backend: $scName")
                            }
                        }
                    } catch (e: Exception) {
                        RFULogger.error("Failed to process backend Sea Creature: $scName", e)
                    }
                }

                val backendKeys = backendConfig.creatures.keys
                val currentKeys = mergedCreatures.keys.toSet()
                currentKeys.forEach { key ->
                    if (!backendKeys.contains(key)) {
                        mergedCreatures.remove(key)
                        deletedCount++
                        RFULogger.dev("Removed Sea Creature not in backend: $key")
                    }
                }

                if (addedCount == 0 && updatedCount == 0 && deletedCount == 0) return@getRequest

                val newSettings = SeaCreatureSettings(mergedCreatures)

                try {
                    val field: Field = JsonFile::class.java.getDeclaredField("data")
                    field.isAccessible = true
                    field.set(seaCreatureConfigFile, newSettings)
                    RFULogger.info("Backend SC sync complete: $addedCount added, $updatedCount updated, $deletedCount deleted")
                    seaCreatureConfigFile.save()

                    newSettings.creatures.keys.forEach { name ->
                        try {
                            registerCreature(name)
                        } catch (e: Exception) {
                            RFULogger.error("Failed to register Sea Creature after backend sync: $name", e)
                        }
                    }
                } catch (e: Exception) {
                    RFULogger.error("Failed to apply backend Sea Creature settings", e)
                    e.printStackTrace()
                }
            }
        }
    }

    override fun instantRegister() {
        seaCreatureConfigFile.data.creatures.keys.forEach { scName ->
            try {
                registerCreature(scName)
            } catch (e: Exception) {
                RFULogger.error("Failed to register Sea Creature: $scName", e)
            }
        }
    }

    override fun register() {
        if (BackendSettings.backendAccepted && BackendSettings.loadScConfigFromBackend) {
            updateFromBackend()
        }
    }

    private fun buildCondition(conditions: SeaCreatureConditions?): (Hotspot?, Vec3, Bait?) -> Boolean {
        if (conditions == null) return { _, _, _ -> true }

        return { hotspot, pos, bait ->
            var result = true

            if (conditions.isFestival == true && !World.isFishingFestival()) result = false
            if (conditions.isSpooky == true && !World.isSpookyFestival()) result = false

            conditions.hotspot?.let { hsType ->
                if (hsType == "WATER" && hotspot?.liquid?.isWater() != true) result = false
                if (hsType == "LAVA" && hotspot?.liquid?.isLava() != true) result = false
            }

            conditions.bait?.let { baitName ->
                if (bait?.name != baitName) result = false
            }

            conditions.coords?.let { ranges ->
                val includeRanges = ranges.filter { it.exclude != true }
                val excludeRanges = ranges.filter { it.exclude == true }

                for (range in excludeRanges) {
                    if (pos.x in range.x0..range.x1 &&
                        pos.y in range.y0..range.y1 &&
                        pos.z in range.z0..range.z1
                    ) {
                        result = false
                        break
                    }
                }
                if (!result) return@let

                if (includeRanges.isNotEmpty()) {
                    var inInclude = false
                    for (range in includeRanges) {
                        if (pos.x in range.x0..range.x1 &&
                            pos.y in range.y0..range.y1 &&
                            pos.z in range.z0..range.z1
                        ) {
                            inInclude = true
                            break
                        }
                    }
                    if (!inInclude) result = false
                }
            }

            result
        }
    }
}
