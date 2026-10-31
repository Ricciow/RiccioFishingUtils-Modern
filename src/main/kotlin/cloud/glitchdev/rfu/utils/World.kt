package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.SeaCreatureCategory
import cloud.glitchdev.rfu.constants.skyblock.Mayors
import cloud.glitchdev.rfu.data.fishing.HotspotCache
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import net.hypixel.data.type.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@AutoRegister
object  World : RegisteredEvent {
    var isInSkyblock = false
        get() {
            return field || (DevSettings.devMode && DevSettings.isInSkyblock)
        }
    var lobby : String? = null
    var island : FishingIslands? = null
    var isOnAlpha: Boolean = false
        private set

    var isOnHypixel: Boolean = false
        private set
        get() = DevSettings.bypassHypixelCheck || field

    private var spookyFestivalDay = -1L
    private var fishingFestivalDay = -1L
    private const val SKYBLOCK_EPOCH = 1560275700000L
    private const val DAY_DURATION_MS = 20L * 60 * 1000L
    private const val HOUR_DURATION_MS = DAY_DURATION_MS / 24
    private const val MINUTE_DURATION_MS = HOUR_DURATION_MS / 60
    private const val MONTH_DURATION_MS = 31 * DAY_DURATION_MS
    private const val YEAR_DURATION_MS = 12 * MONTH_DURATION_MS

    fun getTimeSinceEpoch(): Long {
        return Clock.System.now().toEpochMilliseconds() - SKYBLOCK_EPOCH
    }

    val SBYear: Long
        get() = getTimeSinceEpoch() / YEAR_DURATION_MS + 1

    val SBMonth: Int
        get() = ((getTimeSinceEpoch() % YEAR_DURATION_MS) / MONTH_DURATION_MS).toInt() + 1

    val SBDay: Int
        get() = ((getTimeSinceEpoch() % MONTH_DURATION_MS) / DAY_DURATION_MS).toInt() + 1

    private val SBDayIndex: Long
        get() = getTimeSinceEpoch() / DAY_DURATION_MS

    val SBHour: Int
        get() = ((getTimeSinceEpoch() % DAY_DURATION_MS) / HOUR_DURATION_MS).toInt()

    val SBMinute: Int
        get() = ((getTimeSinceEpoch() % HOUR_DURATION_MS) / MINUTE_DURATION_MS).toInt()

    private fun isDetectedFestival(day: Long): Boolean {
        if (day < 0) return false
        val startsAt = day * DAY_DURATION_MS
        return getTimeSinceEpoch() in startsAt until startsAt + DAY_DURATION_MS + 1.minutes.inWholeMilliseconds
    }

    fun isSpookyFestival(): Boolean = (SBMonth == 8 && SBDay >= 26) || (SBMonth == 9 && SBDay <= 3) ||
            isDetectedFestival(spookyFestivalDay)

    fun isFishingFestival(): Boolean = (SBDay <= 3 && MayorTracker.currentMayor == Mayors.MARINA) ||
            isDetectedFestival(fishingFestivalDay)

    fun isNether(level: Level?): Boolean {
        if (level == null) return false
        return level.dimension() == Level.NETHER ||
                level.dimensionTypeRegistration().`is`(BuiltinDimensionTypes.NETHER) ||
                island == FishingIslands.ISLE ||
                (level.dimensionType().hasCeiling && !level.dimensionType().hasSkyLight)
    }

    override fun register() {
        registerLocationEvent(-1) { event ->
            isInSkyblock = event.serverType.getOrNull() == GameType.SKYBLOCK
            lobby = event.serverName
            val islandName = event.map.getOrElse {
                if (island != null) {
                    HotSpotEvents.clearHotspots()
                    HotspotCache.clearSessionBuffs()
                }
                island = null
                return@registerLocationEvent
            }

            val newIsland = FishingIslands.findIslandObject(islandName)
            if (newIsland != island) {
                HotSpotEvents.clearHotspots()
                HotspotCache.clearSessionBuffs()
            }
            island = newIsland
        }

        registerSeaCreatureCatchEvent(priority = -100) { sc, _, _, _, _ ->
            if (!isInSkyblock || isOnAlpha) return@registerSeaCreatureCatchEvent
            when (sc.category) {
                SeaCreatureCategory.SPOOKY -> spookyFestivalDay = SBDayIndex
                SeaCreatureCategory.SHARK -> fishingFestivalDay = SBDayIndex
                else -> Unit
            }
        }

        registerJoinEvent(priority = -100) { wasConnected ->
            if (!wasConnected) {
                val ip = mc.currentServer?.ip?.lowercase()
                val isAlpha = ip != null && (ip == "alpha.hypixel.net" || ip.endsWith(".alpha.hypixel.net"))

                if (isAlpha) {
                    isOnAlpha = true
                } else {
                    if (isOnAlpha) {
                        RFULogger.info("[RFU] Leaving Alpha server. Rolling back data...")
                        JsonFile.reloadAll()
                    }
                    isOnAlpha = false
                }

                isOnHypixel = ip != null && listOf("hypixel.net", "hypixel.fast").any {
                    ip == it || ip.endsWith(".$it")
                }
            }
        }

        registerDisconnectEvent(priority = -100) {
            spookyFestivalDay = -1L
            fishingFestivalDay = -1L
            if (isOnAlpha) {
                RFULogger.info("[RFU] Disconnected from Alpha server. Rolling back data...")
                JsonFile.reloadAll()
            }
            isOnAlpha = false
            isOnHypixel = false
        }
    }
}
