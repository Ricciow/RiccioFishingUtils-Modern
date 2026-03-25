package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.Mayors
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import net.hypixel.data.type.GameType
import java.time.Clock
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import net.minecraft.network.chat.Component
import kotlin.jvm.optionals.getOrDefault


@AutoRegister
object World : RegisteredEvent {
    var isInSkyblock = false
        get() {
            return field || (DevSettings.devMode && DevSettings.isInSkyblock)
        }
    var lobby : String? = null
    var island : FishingIslands? = null
    var map: String? = null

    private const val SKYBLOCK_EPOCH = 1560275700000L
    private const val DAY_DURATION_MS = 20L * 60 * 1000L
    private const val MONTH_DURATION_MS = 31 * DAY_DURATION_MS
    private const val YEAR_DURATION_MS = 12 * MONTH_DURATION_MS
    private val clock: Clock = Clock.systemUTC()

    fun getTimeSinceEpoch(): Long {
        return clock.millis() - SKYBLOCK_EPOCH
    }

    fun getCurrentSkyBlockYear(): Long {
        return getTimeSinceEpoch() / YEAR_DURATION_MS + 1
    }

    fun getCurrentSkyBlockMonth(): Int {
        return ((getTimeSinceEpoch() % YEAR_DURATION_MS) / MONTH_DURATION_MS).toInt() + 1
    }

    fun getCurrentSkyBlockDay(): Int {
        return ((getTimeSinceEpoch() % MONTH_DURATION_MS) / DAY_DURATION_MS).toInt() + 1
    }

    /**
     * Checks if the current SkyBlock time is during the Spooky Festival.
     * The festival lasts from the 26th of Late Summer to the 3rd of Early Winter.
     * Late Summer is Month 7, Autumn is Month 8, Early Winter is Month 9.
     */
    fun isSpookyFestival(): Boolean {
        val month = getCurrentSkyBlockMonth()
        val day = getCurrentSkyBlockDay()

        return when (month) {
            7 -> day >= 26
            8 -> true
            9 -> day <= 3
            else -> false
        }
    }

    /**
     * Checks if the current SkyBlock time is during the Fishing Festival.
     * The festival lasts for the first 3 days of every month, but only if Marina is Mayor.
     */
    fun isFishingFestival(): Boolean {
        return getCurrentSkyBlockDay() <= 3 && MayorTracker.currentMayor == Mayors.MARINA
    }

    override fun register() {
        registerLocationEvent(-1) { event ->
            isInSkyblock = event.serverType.getOrNull() == GameType.SKYBLOCK
            lobby = event.serverName
            map = event.map.getOrNull()

            val islandName = event.map.getOrElse {
                island = null
                return@registerLocationEvent
            }

            island = FishingIslands.findIslandObject(islandName)
        }
    }
}