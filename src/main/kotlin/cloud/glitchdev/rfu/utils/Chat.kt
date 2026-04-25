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
    var isSendingModCommand = false
    private val commandCooldown = 1000

    override fun register() {
        registerSendCommandEvent { _ ->
            lastMessage = Clock.System.now()
            true
        }
    }

    private fun processQueue() {
        synchronized(this) {
            if (isRunning) return
            isRunning = true
        }

        Coroutines.launch {
            try {
                while (true) {
                    val command = synchronized(commandQueue) {
                        commandQueue.removeFirstOrNull()
                    } ?: break

                    if (command.startsWith("pc ") && !Party.inParty) {
                        continue
                    }

                    val timeSince = (Clock.System.now() - lastMessage).inWholeMilliseconds
                    val waitTime = commandCooldown - timeSince

                    if (waitTime > 0) {
                        delay(waitTime)
                    }

                    lastMessage = Clock.System.now()
                    mc.execute {
                        isSendingModCommand = true
                        try {
                            mc.connection?.sendCommand(command)
                        } finally {
                            isSendingModCommand = false
                        }
                    }
                }
            } finally {
                synchronized(this@Chat) {
                    isRunning = false
                }
            }
        }
    }

    fun sendCommand(command : String) {
        synchronized(commandQueue) {
            commandQueue.add(command)
        }
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