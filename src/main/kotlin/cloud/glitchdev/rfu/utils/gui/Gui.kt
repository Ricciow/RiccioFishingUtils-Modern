package cloud.glitchdev.rfu.utils.gui

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.universal.UScreen
import net.minecraft.client.gui.screens.Screen

@AutoRegister
object Gui : RegisteredEvent {
    private var queuedInterface : Screen? = null

    override fun register() {
        registerTickEvent(-1) { _ ->
            if (queuedInterface != null) {
                UScreen.displayScreen(queuedInterface)
                queuedInterface = null
            }
        }
    }

    fun openGui(gui: Screen) {
        if(queuedInterface == null) {
            queuedInterface = gui
        } else {
            RFULogger.warn("Tried to open a screen while one was already queued")
        }
    }
}