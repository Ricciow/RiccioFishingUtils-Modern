package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.BobbinTimeDisplay
import net.minecraft.world.entity.projectile.FishingHook
import kotlin.math.min
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
        get() {
            val effectiveBobbers = min(bobberCount, 5)
            return effectiveBobbers * totalRatePerBobber
        }

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            updateState()
        }
        registerArmorChangeEvent {
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

        val playerPos = player.position()
        bobberCount = world.entitiesForRendering()
            .filterIsInstance<FishingHook>()
            .count { bobber ->
                val dx = abs(bobber.x - playerPos.x)
                val dy = abs(bobber.y - playerPos.y)
                val dz = abs(bobber.z - playerPos.z)
                dx <= 30.4 && dy <= 30.4 && dz <= 30.4
            }

        BobbinTimeDisplay.updateState()
    }
}
