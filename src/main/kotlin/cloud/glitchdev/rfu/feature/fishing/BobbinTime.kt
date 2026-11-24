package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.ArmorSet
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.inventory.ArmorEvents
import cloud.glitchdev.rfu.events.managers.inventory.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.lifecycle.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.BobbinTimeDisplay
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

@RFUFeature
object BobbinTime : Feature {
    var bobberCount = 0
        private set
    var totalRatePerBobber = 0.0
        private set
    var hasBobbinTimeArmor = false
        private set
    val buffPercentage: Double
        get() = calculateBuffPercentage(bobberCount, totalRatePerBobber)

    override fun onInitialize(scope: EventScope) {
        scope.registerTickEvent(interval = 20) {
            updateState()
        }
        scope.registerArmorChangeEvent {
            updateArmorRates()
        }
        updateArmorRates()
    }

    private fun updateArmorRates() {
        totalRatePerBobber = ArmorEvents.currentArmorSet.bobbinTimeRate
        hasBobbinTimeArmor = ArmorEvents.currentArmorSet.hasBobbinTimeArmor
        BobbinTimeDisplay.updateState()
    }

    fun updateState() {
        val player = mc.player
        val world = mc.level

        if (player == null || world == null) {
            bobberCount = 0
            totalRatePerBobber = 0.0
            hasBobbinTimeArmor = false
            BobbinTimeDisplay.updateState()
            return
        }

        bobberCount = countNearbyBobbers(world, player.position())
        BobbinTimeDisplay.updateState()
    }

    fun calculateCurrentBuffPercentage(player: Player): Double? {
        val world = mc.level ?: return null
        val armor = ArmorSet.fromPlayer(player)
        if (!armor.hasBobbinTimeArmor) return null

        return calculateBuffPercentage(countNearbyBobbers(world, player.position()), armor.bobbinTimeRate)
    }

    private fun calculateBuffPercentage(bobbers: Int, ratePerBobber: Double): Double =
        bobbers.coerceAtMost(5) * ratePerBobber

    private fun countNearbyBobbers(world: ClientLevel, playerPos: Vec3): Int =
        world.entitiesForRendering()
            .filterIsInstance<FishingHook>()
            .count { bobber ->
                val dx = abs(bobber.x - playerPos.x)
                val dy = abs(bobber.y - playerPos.y)
                val dz = abs(bobber.z - playerPos.z)
                dx <= 30.4 && dy <= 30.4 && dz <= 30.4
            }
}
