package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
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

    fun registerSlotClickedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (slot: Slot, screen: AbstractContainerScreen<*>) -> Unit
    ): SlotClickedEvent {
        return SlotClickedEvent(priority, callback).register()
    }

    @ScopedRegistration
    fun registerSlotClicked(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (slot: Slot, title: String) -> Unit
    ): SlotClickedEvent {
        return registerSlotClickedEvent(priority) { slot, screen ->
            callback(slot, screen.title.toUnformattedString())
        }
    }

    class SlotClickedEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (slot: Slot, screen: AbstractContainerScreen<*>) -> Unit
    ) : ManagedTask<(slot: Slot, screen: AbstractContainerScreen<*>) -> Unit, SlotClickedEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
