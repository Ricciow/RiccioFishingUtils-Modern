package cloud.glitchdev.rfu.utils

import gg.essential.elementa.WindowScreen
import gg.essential.universal.UScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object Gui {
    private var queuedInterface : WindowScreen? = null
    private var shouldOpen = false

    fun initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { _ ->
            if (shouldOpen && queuedInterface != null) {
                shouldOpen = false
                UScreen.displayScreen(queuedInterface)
            }
        })
    }

    fun openGui(gui: WindowScreen) {
        if(!shouldOpen) {
            queuedInterface = gui
            shouldOpen = true
        }
        else {
            throw Exception("Tried to open a screen while one was already queued")
        }
    }
}