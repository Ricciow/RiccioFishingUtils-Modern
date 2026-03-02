package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.world.inventory.Slot

object SlotClickedEvents : AbstractEventManager<(Slot) -> Unit, SlotClickedEvents.SlotClickedEvent>() {
    override val runTasks: (Slot) -> Unit = { slot ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(slot)
            }
        }
    }

    fun registerSlotClickedEvent(priority: Int = 20, callback: (Slot) -> Unit): SlotClickedEvent {
        return SlotClickedEvent(priority, callback).register()
    }

    class SlotClickedEvent(
        priority: Int = 20,
        callback: (Slot) -> Unit
    ) : ManagedTask<(Slot) -> Unit, SlotClickedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}