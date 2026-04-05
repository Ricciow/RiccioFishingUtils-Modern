package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.client.gui.screens.packs.PackSelectionScreen

object PackSelectionScreenEvents : AbstractEventManager<(PackSelectionScreen) -> Unit, PackSelectionScreenEvents.PackSelectionScreenTickEvent>() {
    override val runTasks: (PackSelectionScreen) -> Unit = { screen ->
        safeExecution {
            tasks.forEach { it.callback(screen) }
        }
    }

    fun registerPackSelectionScreenTickEvent(priority: Int = 20, callback: (PackSelectionScreen) -> Unit): PackSelectionScreenTickEvent {
        return PackSelectionScreenTickEvent(priority, callback).register()
    }

    class PackSelectionScreenTickEvent(
        priority: Int = 20,
        callback: (PackSelectionScreen) -> Unit
    ) : ManagedTask<(PackSelectionScreen) -> Unit, PackSelectionScreenTickEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
