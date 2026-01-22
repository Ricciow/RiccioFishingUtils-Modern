package cloud.glitchdev.rfu.utils.gui

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import gg.essential.universal.UScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screen.Screen

@AutoRegister
object Gui : RegisteredEvent {
    private var queuedInterface : Screen? = null
    private var shouldOpen = false

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { _ ->
            if (shouldOpen && queuedInterface != null) {
                shouldOpen = false
                UScreen.displayScreen(queuedInterface)
            }
        })
    }

    fun openGui(gui: Screen) {
        if(!shouldOpen) {
            queuedInterface = gui
            shouldOpen = true
        }
        else {
            throw Exception("Tried to open a screen while one was already queued")
        }
    }
}