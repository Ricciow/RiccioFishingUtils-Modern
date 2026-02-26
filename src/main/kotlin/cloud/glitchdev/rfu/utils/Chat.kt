package cloud.glitchdev.rfu.utils

import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Chat {
    private val queue: ArrayDeque<String> = ArrayDeque()
    private var isRunning = false
    
    private fun sendPartyMessages() {
        if (isRunning) return
        isRunning = true

        CoroutineScope(Dispatchers.Default).launch {
            while (queue.isNotEmpty()) {
                sendCommand("pc ${queue.removeFirst()}")
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
            mc.player?.displayClientMessage(message, false)
        }
    }

}