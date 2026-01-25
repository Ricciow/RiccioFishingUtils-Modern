package cloud.glitchdev.rfu.utils

import net.minecraft.text.Text
import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft

object Chat {
    fun sendServerMessage(message : String) {
        minecraft.networkHandler?.sendChatMessage(message)
    }

    fun sendServerCommand(command : String) {
        minecraft.networkHandler?.sendChatCommand(command)
    }

    fun sendMessage(message : Text) {
        minecraft.player?.sendMessage(message, false)
    }
}