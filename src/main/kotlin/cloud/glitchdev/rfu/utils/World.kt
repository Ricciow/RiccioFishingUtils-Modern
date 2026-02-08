package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import net.hypixel.data.type.GameType
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