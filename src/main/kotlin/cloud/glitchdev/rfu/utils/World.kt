package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import net.hypixel.data.type.GameType
import java.time.Clock
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@AutoRegister
object World : RegisteredEvent {
    var isInSkyblock = false
        get() {
            return field || (DevSettings.devMode && DevSettings.isInSkyblock)
        }
    var lobby : String? = null
    var island : FishingIslands? = null
    private const val SKYBLOCK_EPOCH = 1560275700000L
    private const val YEAR_DURATION_MS = 124L * 60 * 60 * 1000L
    private val clock: Clock = Clock.systemUTC()

    fun getCurrentSkyBlockYear(): Long {
        val currentTime = clock.millis()
        return (currentTime - SKYBLOCK_EPOCH) / YEAR_DURATION_MS + 1
    }

    override fun register() {
        registerLocationEvent(-1) { event ->
            isInSkyblock = event.serverType.getOrNull() == GameType.SKYBLOCK
            lobby = event.serverName

            val islandName = event.map.getOrElse {
                island = null
                return@registerLocationEvent
            }

            island = FishingIslands.findIslandObject(islandName)
        }
    }
}