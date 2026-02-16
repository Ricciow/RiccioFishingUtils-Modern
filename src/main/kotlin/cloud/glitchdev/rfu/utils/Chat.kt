package cloud.glitchdev.rfu.utils

import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.RiccioFishingUtils.mc

object Chat {
    fun sendServerMessage(message : String) {
        mc.connection?.sendChat(message)
    }

    fun sendCommand(command : String) {
        mc.connection?.sendCommand(command)
    }

    fun sendMessage(message : Component) {
        //Ensure it's in the render thread.
        mc.execute {
            mc.player?.displayClientMessage(message, false)
        }
    }
}