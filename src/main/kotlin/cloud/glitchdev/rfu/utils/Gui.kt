package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.gui.components.dropdown.DropdownOption
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import java.awt.Color

object Gui {
    private var queuedInterface : WindowScreen? = null
    private var shouldOpen = false

    fun initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
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

    val transparent : Color = Color(0, 0,0, 0)
}