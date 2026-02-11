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
        mc.player?.displayClientMessage(message, false)
    }
}