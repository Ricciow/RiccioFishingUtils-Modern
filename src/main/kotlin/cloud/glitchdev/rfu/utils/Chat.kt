package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text
import cloud.glitchdev.rfu.RiccioFishingUtils.Companion.minecraft

object Chat {
    fun registerChat(listener: (message : Text) -> Unit) {
        registerChatOnly(listener)
        registerGame(listener)
    }

    fun registerChat(filter : Regex, listener: (message : Text, matchGroups : List<String>) -> Unit) {
        registerChat { message ->
            val matchResults = filter.find(message.string.removeFormatting())
            if (matchResults != null) {
                listener(message, matchResults.groupValues)
            }
        }
    }

    fun registerGame(listener: (message : Text) -> Unit) {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            listener(message)
        }
    }

    fun registerChatOnly(listener: (message : Text) -> Unit) {
        ClientReceiveMessageEvents.CHAT.register { message, _, _, _, _->
            listener(message)
        }
    }

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