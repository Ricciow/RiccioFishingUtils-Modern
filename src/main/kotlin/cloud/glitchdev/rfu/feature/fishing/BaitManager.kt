package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.events.managers.BobberLiquidEvents.registerBobberLiquidEvent
import cloud.glitchdev.rfu.events.managers.EntityAddedEvents.registerEntityAddedEvent
import cloud.glitchdev.rfu.events.managers.EntityDataEvents.registerEntityDataEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.FishingHook

@RFUFeature
object BaitManager : Feature {
    var lastBait: Bait? = null

    override fun onInitialize() {
        registerEntityAddedEvent { entity ->
            val player = mc.player ?: return@registerEntityAddedEvent

            // Reset bait when player casts a new bobber
            if (entity is FishingHook && entity.owner == player) {
                lastBait = null
            }
        }

        registerEntityDataEvent { entity ->
            if (entity !is ItemEntity) return@registerEntityDataEvent
            val player = mc.player ?: return@registerEntityDataEvent
            val bobber = player.fishing ?: return@registerEntityDataEvent

            val stack = entity.item
            if (stack.isEmpty) return@registerEntityDataEvent

            val bait = Bait.fromName(stack.hoverName.string) ?: return@registerEntityDataEvent

            if (entity.distanceTo(bobber) < 5.0) {
                lastBait = bait
            }
        }


        registerBobberLiquidEvent { bobber ->
            val itemBait = findBaitNear(bobber)
            if (itemBait != null) {
                lastBait = itemBait
                return@registerBobberLiquidEvent
            }

            if (lastBait == null) {
                lastBait = findBaitInInventory()
            }
        }
    }

    private fun findBaitNear(bobber: FishingHook): Bait? {
        val speed = bobber.deltaMovement.length()
        val inflation = (speed * 5.0).coerceAtLeast(1.0)
        val aabb = bobber.boundingBox.inflate(inflation)
        val itemsNearBobber = mc.level?.getEntitiesOfClass(ItemEntity::class.java, aabb)

        itemsNearBobber?.forEach { itemEntity ->
            val stack = itemEntity.item
            if (stack.isEmpty) return@forEach
            Bait.fromName(stack.hoverName.string)?.let {
                return it
            }
        }
        return null
    }

    private fun findBaitInInventory(): Bait? {
        val player = mc.player ?: return null
        for (i in 0 until player.inventory.containerSize) {
            val stack = player.inventory.getItem(i)
            if (stack.isEmpty) continue
            Bait.fromName(stack.hoverName.string)?.let {
                return it
            }
        }
        return null
    }
}
