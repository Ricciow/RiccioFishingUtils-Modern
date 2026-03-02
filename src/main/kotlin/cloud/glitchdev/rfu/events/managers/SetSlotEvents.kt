package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.world.item.ItemStack

object SetSlotEvents : AbstractEventManager<(containerId: Int, slot: Int, item: ItemStack) -> Unit, SetSlotEvents.SetSlotEvent>() {
    override val runTasks: (Int, Int, ItemStack) -> Unit = { containerId, slot, item ->
        safeExecution {
            tasks.forEach { event -> event.callback(containerId, slot, item) }
        }
    }

    fun registerSetSlotEvent(
        priority: Int = 20,
        callback: (containerId: Int, slot: Int, item: ItemStack) -> Unit
    ): SetSlotEvent {
        return SetSlotEvent(priority, callback).register()
    }

    class SetSlotEvent(
        priority: Int = 20,
        callback: (containerId: Int, slot: Int, item: ItemStack) -> Unit
    ) : ManagedTask<(containerId: Int, slot: Int, item: ItemStack) -> Unit, SetSlotEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
