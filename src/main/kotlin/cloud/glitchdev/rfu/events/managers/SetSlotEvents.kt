package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.world.item.ItemStack

@EventManager(register = false)
object SetSlotEvents : AbstractEventManager<(containerId: Int, slot: Int, item: ItemStack) -> Unit, SetSlotEvents.SetSlotEvent>() {
    override val runTasks: (Int, Int, ItemStack) -> Unit = { containerId, slot, item ->
        safeExecution {
            tasks.forEach { event -> event.callback(containerId, slot, item) }
        }
    }

    fun registerSetSlotEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (containerId: Int, slot: Int, item: ItemStack) -> Unit
    ): SetSlotEvent {
        return SetSlotEvent(priority, callback).register()
    }

    class SetSlotEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (containerId: Int, slot: Int, item: ItemStack) -> Unit
    ) : ManagedTask<(containerId: Int, slot: Int, item: ItemStack) -> Unit, SetSlotEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
