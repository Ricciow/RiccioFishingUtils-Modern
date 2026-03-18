package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.data.fishing.Bait
import cloud.glitchdev.rfu.events.managers.BobberLiquidEvents.registerBobberLiquidEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import net.minecraft.world.entity.item.ItemEntity

@RFUFeature
object BaitManager : Feature {
    var lastBait: Bait? = null

    override fun onInitialize() {
        registerBobberLiquidEvent { bobber ->
            val player = mc.player ?: return@registerBobberLiquidEvent

            val aabb = bobber.boundingBox.inflate(1.0)
            val itemsNearBobber = mc.level?.getEntitiesOfClass(ItemEntity::class.java, aabb)
            
            var foundBait: Bait? = null

            itemsNearBobber?.forEach { itemEntity ->
                val stack = itemEntity.item
                Bait.fromName(stack.hoverName.string)?.let {
                    foundBait = it
                    return@forEach
                }
            }

            if (foundBait == null) {
                for (i in 0 until player.inventory.containerSize) {
                    val stack = player.inventory.getItem(i)
                    Bait.fromName(stack.hoverName.string)?.let {
                        foundBait = it
                        break
                    }
                }
            }

            if (foundBait != null) {
                lastBait = foundBait
            }
        }
    }
}
