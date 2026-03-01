package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack

@AutoRegister
object ItemUsedEvents : AbstractEventManager<(item: ItemStack) -> Unit, ItemUsedEvents.ItemUsedEvent>(), RegisteredEvent {
    override fun register() {
        UseItemCallback.EVENT.register { player, _, hand ->
            val item = player.getItemInHand(hand)

            runTasks(item)

            InteractionResult.PASS
        }
    }

    fun runTasks(item: ItemStack) {
        safeExecution {
            tasks.forEach { task -> task.callback(item) }
        }
    }

    fun registerItemUsedEvent(
        priority: Int = 20,
        callback: (item: ItemStack) -> Unit
    ): ItemUsedEvent {
        return ItemUsedEvent(priority, callback).register()
    }

    class ItemUsedEvent(
        priority: Int = 20,
        callback: (item: ItemStack) -> Unit
    ) : ManagedTask<(item: ItemStack) -> Unit, ItemUsedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}