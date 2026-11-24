package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack

@EventManager
object ItemUsedEvents : AbstractEventManager<(item: ItemStack) -> Unit, ItemUsedEvents.ItemUsedEvent>(), RegisteredEvent {
    override fun register(scope: EventScope) {
        UseItemCallback.EVENT.register { player, _, hand ->
            val item = player.getItemInHand(hand)

            runTasks(item)

            InteractionResult.PASS
        }
    }

    override val runTasks: (ItemStack) -> Unit = { item ->
        safeExecution {
            tasks.forEach { task -> task.callback(item) }
        }
    }

    class ItemUsedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (item: ItemStack) -> Unit
    ) : ManagedTask<(item: ItemStack) -> Unit, ItemUsedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}