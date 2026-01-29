package cloud.glitchdev.rfu.utils.gui

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.universal.UScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screen.Screen

@AutoRegister
object Gui : RegisteredEvent {
    private var queuedInterface : Screen? = null
    private var shouldOpen = false

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            if (shouldOpen && queuedInterface != null) {
                shouldOpen = false
                UScreen.displayScreen(queuedInterface)
            }
        }
    }

    fun openGui(gui: Screen) {
        if(!shouldOpen) {
            queuedInterface = gui
            shouldOpen = true
        }
        else {
            RFULogger.warn("Tried to open a screen while one was already queued")
        }
    }
}