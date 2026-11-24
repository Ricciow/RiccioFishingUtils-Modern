package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.Slot

@EventManager(register = false)
object SlotRenderEvents : AbstractEventManager<(GuiGraphicsExtractor, AbstractContainerScreen<*>, Slot) -> Unit, SlotRenderEvents.SlotRenderEvent>() {
    override val runTasks: (GuiGraphicsExtractor, AbstractContainerScreen<*>, Slot) -> Unit = { graphics, screen, slot ->
        safeExecution {
            tasks.forEach { task -> task.callback(graphics, screen, slot) }
        }
    }

    class SlotRenderEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (GuiGraphicsExtractor, AbstractContainerScreen<*>, Slot) -> Unit
    ) : ManagedTask<(GuiGraphicsExtractor, AbstractContainerScreen<*>, Slot) -> Unit, SlotRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
