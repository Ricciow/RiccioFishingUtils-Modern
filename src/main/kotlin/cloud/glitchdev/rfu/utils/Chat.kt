package cloud.glitchdev.rfu.utils

import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerSendCommandEvent
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Instant

@AutoRegister
object Chat : RegisteredEvent {
    private val queue: ArrayDeque<String> = ArrayDeque()
    private var isRunning = false
    private var lastMessage = Instant.DISTANT_PAST
    var isSendingModMessage = false

    override fun register() {
        registerSendCommandEvent { message ->
            if(message.startsWith("pc ")) {
                lastMessage = Clock.System.now()
            }
            true
        }
    }

    private fun sendPartyMessages() {
        if (isRunning) return
        isRunning = true

        Coroutines.launch {
            while (queue.isNotEmpty()) {
                val timeSince = (Clock.System.now() - lastMessage).inWholeMilliseconds
                delay(maxOf(500 - timeSince, 0))

                if(Party.inParty) {
                    sendCommand("pc ${queue.removeFirst()}")
                } else {
                    queue.removeFirst()
                    continue
                }
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
        if (message.length > 240) {
            val chunks = message.chunked(230)
            chunks.forEachIndexed { index, chunk ->
                queue.add("(${index + 1}/${chunks.size}) $chunk")
            }
        } else {
            queue.add(message)
        }
        sendPartyMessages()
    }

    fun sendMessage(message : Component) {
        //Ensure it's in the render thread.
        mc.execute {
            isSendingModMessage = true
            try {
                //? if >=26.1 {
                mc.player?.sendSystemMessage(message)
                //?} else {
                /*mc.player?.displayClientMessage(message, false)
                *///?}
            } finally {
                isSendingModMessage = false
            }
        }
    }
}