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
    private val commandQueue: ArrayDeque<String> = ArrayDeque()
    private var isRunning = false
    private var lastMessage = Instant.DISTANT_PAST
    var isSendingModMessage = false

    override fun register() {
        registerSendCommandEvent { _ ->
            lastMessage = Clock.System.now()
            true
        }
    }

    private fun processQueue() {
        if (isRunning) return
        isRunning = true

        Coroutines.launch {
            while (commandQueue.isNotEmpty()) {
                val nextCommand = commandQueue.first()
                val timeSince = (Clock.System.now() - lastMessage).inWholeMilliseconds
                val waitTime = 500 - timeSince

                if (waitTime > 0) {
                    delay(waitTime)
                }

                val command = commandQueue.removeFirst()

                if (nextCommand.startsWith("pc ") && !Party.inParty) {
                    continue
                }

                mc.execute {
                    mc.connection?.sendCommand(command)
                    lastMessage = Clock.System.now()
                }
            }
            isRunning = false
        }
    }
    
    fun sendServerMessage(message : String) {
        mc.connection?.sendChat(message)
    }

    fun sendCommand(command : String) {
        commandQueue.add(command)
        processQueue()
    }

    fun sendPartyMessage(message : String) {
        if (message.length > 240) {
            val chunks = message.chunked(230)
            chunks.forEachIndexed { index, chunk ->
                sendCommand("pc (${index + 1}/${chunks.size}) $chunk")
            }
        } else {
            sendCommand("pc $message")
        }
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