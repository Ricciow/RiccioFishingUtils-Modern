package cloud.glitchdev.rfu.utils

import net.minecraft.network.chat.Component
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.InstantEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAnyChatEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerSendCommandEvent
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@AutoRegister
object Chat : RegisteredEvent {
    const val MUTE_SAVE_FIELD = "mute_expiration"
    val MUTE_REGEX = """Your mute will expire in\s+(?:(\d+)\s*d)?\s*(?:(\d+)\s*h)?\s*(?:(\d+)\s*m)?\s*(?:(\d+)\s*s)?""".toRegex()
    private val CHAT_COMMAND_PREFIXES = listOf("pc ", "gc ", "ac ", "oc ", "cc ", "msg ", "w ", "tell ", "r ")

    private val commandQueue: ArrayDeque<String> = ArrayDeque()
    private var isRunning = false
    private var lastMessage = Instant.DISTANT_PAST
    var isSendingModMessage = false
    var isSendingModCommand = false
    private val commandCooldown = 1000

    val isMuted: Boolean
        get() = muteExpiration != null

    var muteExpiration: Instant?
        get() {
            val expireTime = (OtherManager.getField(MUTE_SAVE_FIELD) as? InstantEntry)?.value ?: return null
            return if (Clock.System.now() < expireTime) expireTime else null
        }
        set(value) {
            OtherManager.setField(MUTE_SAVE_FIELD, InstantEntry(value))
            OtherManager.file.save()
        }

    override fun register() {
        registerSendCommandEvent { _ ->
            lastMessage = Clock.System.now()
            true
        }

        registerAnyChatEvent(MUTE_REGEX) { _, matches ->
            val groups = matches?.groupValues ?: return@registerAnyChatEvent
            val days = groups.getOrNull(1)?.toLongOrNull() ?: 0L
            val hours = groups.getOrNull(2)?.toLongOrNull() ?: 0L
            val minutes = groups.getOrNull(3)?.toLongOrNull() ?: 0L
            val seconds = groups.getOrNull(4)?.toLongOrNull() ?: 0L

            val totalSeconds = days * 86400L + hours * 3600L + minutes * 60L + seconds
            if (totalSeconds > 0) {
                val expiresAt = Clock.System.now() + totalSeconds.seconds
                muteExpiration = expiresAt
                RFULogger.dev("Mute detected, expires at: $expiresAt")
                synchronized(commandQueue) {
                    commandQueue.removeAll { isChatMessage(it) }
                }
            }
        }
    }

    fun isChatMessage(command: String): Boolean {
        val trimmed = command.trimStart()
        return CHAT_COMMAND_PREFIXES.any { trimmed.startsWith(it, ignoreCase = true) }
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

                    if (isMuted && isChatMessage(command)) {
                        continue
                    }

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
        if (isMuted && isChatMessage(command)) return
        synchronized(commandQueue) {
            commandQueue.add(command)
        }
        processQueue()
    }

    fun sendPartyMessage(message : String) {
        if (isMuted) return
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
                mc.player?.sendSystemMessage(message)
            } finally {
                isSendingModMessage = false
            }
        }
    }

    fun sendPreviewPartyMessage(message : String) {
        val username = User.getUsername()
        val formatted = "${TextColor.LIGHT_BLUE}Party ${TextColor.DARK_GRAY}> ${TextColor.GRAY}$username${TextColor.WHITE}: $message"
        sendMessage(Component.literal(formatted))
    }
}