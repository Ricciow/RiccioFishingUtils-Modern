package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.BobbinTimeDisplay
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.ItemStack
import kotlin.math.min
import kotlin.math.abs

@RFUFeature
object BobbinTime : Feature {
    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
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

        var rateSum = 0.0
        var foundArmor = false

        for (slot in armorSlots) {
            val armorPiece = player.getItemBySlot(slot)
            val rate = getBobbinTimeRate(armorPiece)
            if (rate != null) {
                rateSum += rate
                foundArmor = true
            }
        }

        totalRatePerBobber = rateSum
        hasBobbinTimeArmor = foundArmor

        BobbinTimeDisplay.updateState()
    }

    private fun getBobbinTimeRate(itemStack: ItemStack): Double? {
        if (itemStack.isEmpty) return null
        val lore = itemStack[DataComponents.LORE] ?: return null
        for (line in lore.lines) {
            val plainText = line.toUnformattedString()
            if (plainText.contains("Bobbin' Time V") || plainText.contains("Bobbin' Time 5")) {
                return 0.01
            }
            if (plainText.contains("Bobbin' Time IV") || plainText.contains("Bobbin' Time 4")) {
                return 0.008
            }
            if (plainText.contains("Bobbin' Time III") || plainText.contains("Bobbin' Time 3")) {
                return 0.006
            }
        }
        return null
    }
}
