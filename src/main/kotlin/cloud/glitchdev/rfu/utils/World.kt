package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.Mayors
import cloud.glitchdev.rfu.constants.SeaCreatureCategory
import cloud.glitchdev.rfu.data.fishing.HotspotCache
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import net.hypixel.data.type.GameType
import java.time.Clock
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
    var jerryFishingFestival = false

    private const val SKYBLOCK_EPOCH = 1560275700000L
    private const val DAY_DURATION_MS = 20L * 60 * 1000L
    private const val HOUR_DURATION_MS = DAY_DURATION_MS / 24
    private const val MINUTE_DURATION_MS = HOUR_DURATION_MS / 60
    private const val MONTH_DURATION_MS = 31 * DAY_DURATION_MS
    private const val YEAR_DURATION_MS = 12 * MONTH_DURATION_MS
    private val mayor
        get() = MayorTracker.currentMayor
    private val clock: Clock = Clock.systemUTC()

    fun getTimeSinceEpoch(): Long {
        return clock.millis() - SKYBLOCK_EPOCH
    }

    val SBYear: Long
        get() = getTimeSinceEpoch() / YEAR_DURATION_MS + 1

    val SBMonth: Int
        get() = ((getTimeSinceEpoch() % YEAR_DURATION_MS) / MONTH_DURATION_MS).toInt() + 1

    val SBDay: Int
        get() = ((getTimeSinceEpoch() % MONTH_DURATION_MS) / DAY_DURATION_MS).toInt() + 1

    val SBHour: Int
        get() = ((getTimeSinceEpoch() % DAY_DURATION_MS) / HOUR_DURATION_MS).toInt()

    val SBMinute: Int
        get() = ((getTimeSinceEpoch() % HOUR_DURATION_MS) / MINUTE_DURATION_MS).toInt()

    /**
     * Checks if the current SkyBlock time is during the Spooky Festival.
     * The festival lasts from the 26th of the 8th month to the 3rd of the 9th month.
     */
    fun isSpookyFestival(): Boolean {
        return when (SBMonth) {
            8 -> SBDay >= 26
            9 -> SBDay <= 3
            else -> false
        }
    }

    /**
     * Checks if the current SkyBlock time is during the Fishing Festival.
     * The festival lasts for the first 3 days of every month, but only if Marina is Mayor.
     */
    fun isFishingFestival(): Boolean {
        if (SBDay <= 3 && jerryFishingFestival) {
            return true
        } else {
            jerryFishingFestival = false
        }
        return SBDay <= 3 && (mayor == Mayors.MARINA)
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

        registerSeaCreatureCatchEvent { sc, _, _, _, _ ->
            if (sc.category == SeaCreatureCategory.SHARK && mayor == Mayors.JERRY) {
                jerryFishingFestival = true
            }
        }
    }
}