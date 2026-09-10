package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAnyChatEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.ServerCountdownEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.registerServerCountdownEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import net.hypixel.data.type.GameType

@RFUFeature
object MobyDuckFeature : Feature {
    private const val MOBY_DURATION = 3600L * 20

    var isCollectorsEdition: Boolean = false
        private set

    var wisdomValue: String = "1"
        private set

    private var serverCountdownTask: ServerCountdownEvent? = null

    val remainingDuration: Duration
        get() = serverCountdownTask?.remainingDuration ?: Duration.ZERO

    val isActive: Boolean
        get() = serverCountdownTask != null && serverCountdownTask!!.remainingTicks > 0L && World.isInSkyblock

    private val mobyDuckRegex = """You consumed a Moby-Duck(?::\s*(Collector's Edition))?\s*and gained \+([\d.]+)[\s☯]*Fishing Wisdom for 60m!""".toExactRegex()
    private val expirationWarningRegex = """Moby-Duck expires in (\d+)s!?""".toExactRegex()
    private val expiredRegex = """Moby-Duck has expired!?""".toExactRegex()

    override fun onInitialize() {
        registerAnyChatEvent(mobyDuckRegex) { _, match ->
            val isCollectors = match?.groupValues?.getOrNull(1)?.isNotEmpty() == true
            val wisdom = match?.groupValues?.getOrNull(2) ?: "1"
            startTimer(MOBY_DURATION, isCollectors, wisdom)
        }

        registerAnyChatEvent(expirationWarningRegex) { _, match ->
            val seconds = match?.groupValues?.getOrNull(1)?.toLongOrNull() ?: return@registerAnyChatEvent
            resyncTimer(seconds * 20L)
        }

        registerAnyChatEvent(expiredRegex) { _, _ ->
            stopTimers()
        }

        registerLocationEvent { event ->
            val serverType = event.serverType.getOrNull()
            if (serverType != null && serverType != GameType.SKYBLOCK) {
                stopTimers()
            }
        }

        registerDisconnectEvent {
            stopTimers()
        }
    }

    fun startTimer(
        durationTicks: Long = MOBY_DURATION,
        isCollectors: Boolean = false,
        wisdom: String = "1"
    ) {
        isCollectorsEdition = isCollectors
        wisdomValue = wisdom

        serverCountdownTask?.unregister()
        serverCountdownTask = registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = 20L,
            onComplete = {
                stopTimers()
            }
        )
    }

    fun resyncTimer(ticks: Long) {
        val currentTask = serverCountdownTask
        if (currentTask != null && !currentTask.isCompleted) {
            currentTask.resync(ticks)
        } else {
            startTimer(ticks, isCollectorsEdition, wisdomValue)
        }
    }

    fun stopTimers() {
        serverCountdownTask?.unregister()
        serverCountdownTask = null
    }
}
