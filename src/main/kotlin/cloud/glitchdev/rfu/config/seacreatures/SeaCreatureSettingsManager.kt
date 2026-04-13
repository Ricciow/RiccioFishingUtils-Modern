package cloud.glitchdev.rfu.config.seacreatures

import cloud.glitchdev.rfu.events.InstantRegister
import cloud.glitchdev.rfu.events.InstantRegisteredEvent
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.SeaCreatureCategory
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.utils.World
import net.minecraft.world.phys.Vec3
import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.utils.network.Network
import com.google.gson.Gson
import java.lang.reflect.Field

@InstantRegister
object SeaCreatureSettingsManager : InstantRegisteredEvent {
    val seaCreatureConfigFile = JsonFile(
        directory = "repo",
        filename = "sc-config.json",
        type = SeaCreatureSettings::class.java,
        defaultFactory = { SeaCreatureSettings.empty() }
    )

    private lateinit var defaultConfig: SeaCreatureSettings
    private val scConfig get() = seaCreatureConfigFile.data

    fun getName(scName: String): String = resolve(scName) { it.name }
    fun getPlural(scName: String): String = resolve(scName) { it.plural }
    fun getArticle(scName: String): String = resolve(scName) { it.article }
    fun getStyle(scName: String): String = resolve(scName) { it.style }
    fun isSpecial(scName: String): Boolean = resolve(scName) { it.special }

    fun save() {
        seaCreatureConfigFile.save()
    }

    fun updateCreature(scName: String, update: (SeaCreatureSetting) -> SeaCreatureSetting) {
        val current = scConfig.creatures[scName] ?: defaultConfig.creatures[scName]!!
        val updated = update(current)

        val creatures = scConfig.creatures.toMutableMap()
        creatures[scName] = updated

        val newSettings = scConfig.copy(creatures = creatures)

        try {
            val field: Field = JsonFile::class.java.getDeclaredField("data")
            field.isAccessible = true
            field.set(seaCreatureConfigFile, newSettings)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sc = SeaCreatures(
            scName = scName,
            catchMessage = updated.catchMessage ?: defaultConfig.creatures[scName]?.catchMessage ?: "",
            liquidType = LiquidTypes.valueOf(updated.liquidType ?: defaultConfig.creatures[scName]?.liquidType ?: "WATER"),
            category = SeaCreatureCategory.valueOf(updated.category ?: defaultConfig.creatures[scName]?.category ?: "GENERAL_WATER"),
            condition = buildCondition(updated.conditions ?: defaultConfig.creatures[scName]?.conditions),
            lsRangeExcluded = updated.lsRangeExcluded ?: defaultConfig.creatures[scName]?.lsRangeExcluded ?: false,
            bossbar = updated.bossbar ?: defaultConfig.creatures[scName]?.bossbar ?: false
        )
        SeaCreatures.register(sc)
    }

    private fun <T> resolve(
        scName: String,
        extractor: (SeaCreatureSetting) -> T?
    ): T {
        val configured = scConfig.creatures[scName]?.let { extractor(it) }
        if (configured != null) return configured
        val default = defaultConfig.creatures[scName]
        return extractor(default!!)!!
    }

    fun updateFromBackend() {
        Network.getRequest("${RiccioFishingUtils.API_URL}/config/sc-config") { response ->
            if (response.isSuccessful()) {
                val gson = Gson()
                val backendConfig = try {
                    gson.fromJson(response.body, SeaCreatureSettings::class.java)
                } catch (e: Exception) {
                    null
                } ?: return@getRequest

                val currentConfig = seaCreatureConfigFile.data
                val mergedCreatures = currentConfig.creatures.toMutableMap()

                backendConfig.creatures.forEach { (scName, backendSc) ->
                    val currentSc = mergedCreatures[scName]
                    if (currentSc == null) {
                        mergedCreatures[scName] = backendSc
                    } else {
                        // Non-customizable: catchMessage, liquidType, category, conditions
                        mergedCreatures[scName] = currentSc.copy(
                            catchMessage = backendSc.catchMessage,
                            liquidType = backendSc.liquidType,
                            category = backendSc.category,
                            conditions = backendSc.conditions
                        )
                    }
                }

                val newSettings = SeaCreatureSettings(mergedCreatures)

                try {
                    val field: Field = JsonFile::class.java.getDeclaredField("data")
                    field.isAccessible = true
                    field.set(seaCreatureConfigFile, newSettings)
                    seaCreatureConfigFile.save()

                    newSettings.creatures.forEach { (name, setting) ->
                        val sc = SeaCreatures(
                            scName = name,
                            catchMessage = setting.catchMessage ?: "",
                            liquidType = LiquidTypes.valueOf(setting.liquidType ?: "WATER"),
                            category = SeaCreatureCategory.valueOf(setting.category ?: "GENERAL_WATER"),
                            condition = buildCondition(setting.conditions),
                            lsRangeExcluded = setting.lsRangeExcluded ?: false,
                            bossbar = setting.bossbar ?: false
                        )
                        SeaCreatures.register(sc)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun instantRegister() {
        val gson = Gson()

        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("assets/rfu/defaults/sc-config.json")
            ?: error("Missing resource: assets/rfu/defaults/sc-config.json")

        defaultConfig = gson.fromJson(stream.bufferedReader(), SeaCreatureSettings::class.java)

        defaultConfig.creatures.forEach { (scName, setting) ->
            val sc = SeaCreatures(
                scName = scName,
                catchMessage = setting.catchMessage ?: "",
                liquidType = LiquidTypes.valueOf(setting.liquidType ?: "WATER"),
                category = SeaCreatureCategory.valueOf(setting.category ?: "GENERAL_WATER"),
                condition = buildCondition(setting.conditions),
                lsRangeExcluded = setting.lsRangeExcluded ?: false,
                bossbar = setting.bossbar ?: false
            )
            SeaCreatures.register(sc)
        }

        updateFromBackend()
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
