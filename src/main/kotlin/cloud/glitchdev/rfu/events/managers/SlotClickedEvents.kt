package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

object SlotClickedEvents : AbstractEventManager<(slot: Slot, screen: AbstractContainerScreen<*>) -> Unit, SlotClickedEvents.SlotClickedEvent>() {
    override val runTasks: (Slot, AbstractContainerScreen<*>) -> Unit = { slot, screen ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(slot, screen)
            }
        }
    }

    fun registerSlotClickedEvent(
        priority: Int = 20,
        callback: (slot: Slot, screen: AbstractContainerScreen<*>) -> Unit
    ): SlotClickedEvent {
        return SlotClickedEvent(priority, callback).register()
    }

    fun registerSlotClicked(
        priority: Int = 20,
        callback: (slot: Slot, title: String) -> Unit
    ): SlotClickedEvent {
        return registerSlotClickedEvent(priority) { slot, screen ->
            callback(slot, screen.title.toUnformattedString())
        }
    }

    class SlotClickedEvent(
        priority: Int = 20,
        callback: (slot: Slot, screen: AbstractContainerScreen<*>) -> Unit
    ) : ManagedTask<(slot: Slot, screen: AbstractContainerScreen<*>) -> Unit, SlotClickedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}