package cloud.glitchdev.rfu.utils

import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import kotlinx.coroutines.delay

object Chat {
    private val queue: ArrayDeque<String> = ArrayDeque()
    private var isRunning = false
    
    private fun sendPartyMessages() {
        if (isRunning) return
        isRunning = true

        Coroutines.launch {
            while (queue.isNotEmpty()) {
                if(Party.inParty) {
                    sendCommand("pc ${queue.removeFirst()}")
                } else {
                    queue.removeFirst()
                    continue
                }
                delay(500)
            }
            isRunning = false
        }
    }
    
    fun sendServerMessage(message : String) {
        mc.connection?.sendChat(message)
    }

    fun sendCommand(command : String) {
        mc.execute {
            mc.connection?.sendCommand(command)
        }
    }

    fun sendPartyMessage(message : String) {
        queue.add(message)
        sendPartyMessages()
    }

    fun sendMessage(message : Component) {
        //Ensure it's in the render thread.
        mc.execute {
            //? if >=26.1 {
            mc.player?.sendSystemMessage(message)
            //?} else {
            /*mc.player?.displayClientMessage(message, false)
            *///?}
        }
    }

}