package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.registration.ScopedRegistration
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

@EventManager(register = false)
object SlotClickedEvents : AbstractEventManager<(slot: Slot, screen: AbstractContainerScreen<*>) -> Unit, SlotClickedEvents.SlotClickedEvent>() {
    override val runTasks: (Slot, AbstractContainerScreen<*>) -> Unit = { slot, screen ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(slot, screen)
            }
        }
    }

    @ScopedRegistration
    internal fun registerSlotClickedTitleEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (slot: Slot, title: String) -> Unit
    ): SlotClickedEvent {
        return SlotClickedEvent(priority) { slot, screen ->
            callback(slot, screen.title.toUnformattedString())
        }.register()
    }

    class SlotClickedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (slot: Slot, screen: AbstractContainerScreen<*>) -> Unit
    ) : ManagedTask<(slot: Slot, screen: AbstractContainerScreen<*>) -> Unit, SlotClickedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
