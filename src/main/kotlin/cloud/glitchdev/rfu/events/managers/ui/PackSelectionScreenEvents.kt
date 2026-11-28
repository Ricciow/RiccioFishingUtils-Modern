package cloud.glitchdev.rfu.events.managers.ui

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.gui.screens.packs.PackSelectionScreen

@EventManager(register = false)
object PackSelectionScreenEvents : AbstractEventManager<(PackSelectionScreen) -> Unit, PackSelectionScreenEvents.PackSelectionScreenTickEvent>() {
    override val runTasks: (PackSelectionScreen) -> Unit = { screen ->
        safeExecution {
            tasks.forEach { it.callback(screen) }
        }
    }

    class PackSelectionScreenTickEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (PackSelectionScreen) -> Unit
    ) : ManagedTask<(PackSelectionScreen) -> Unit, PackSelectionScreenTickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
