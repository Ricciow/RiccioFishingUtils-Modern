package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.MobyDuckEntry
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAnyChatEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.ServerCountdownEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.registerServerCountdownEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlin.time.Duration

@RFUFeature
object MobyDuckFeature : Feature {
    private const val MOBY_DURATION = 3600L * 20L

    private val entry: MobyDuckEntry
        get() = OtherManager.getField("moby_duck") { MobyDuckEntry() } as? MobyDuckEntry ?: MobyDuckEntry()
    val isCollectorsEdition: Boolean
        get() = entry.isCollectorsEdition
    val wisdomValue: String
        get() = entry.wisdomValue

    private var serverCountdownTask: ServerCountdownEvent? = null

    val remainingDuration: Duration
        get() = serverCountdownTask?.remainingDuration ?: Duration.ZERO

    val isActive: Boolean
        get() = (serverCountdownTask?.remainingTicks ?: entry.remainingTicks) > 0L && World.isInSkyblock

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

        registerLocationEvent {
            if (World.isInSkyblock) {
                unpauseTimer()
            } else {
                pauseTimer()
            }
        }

        registerDisconnectEvent {
            pauseTimer()
        }

        if (World.isInSkyblock && entry.remainingTicks > 0L) {
            unpauseTimer()
        }
    }

    fun startTimer(
        durationTicks: Long = MOBY_DURATION,
        isCollectors: Boolean = false,
        wisdom: String = "1"
    ) {
        if (!World.isInSkyblock) return

        entry.isCollectorsEdition = isCollectors
        entry.wisdomValue = wisdom
        entry.remainingTicks = durationTicks
        OtherManager.file.save()

        serverCountdownTask?.unregister()
        serverCountdownTask = registerServerCountdownEvent(
            durationTicks = durationTicks,
            interval = 20L,
            onTick = { task ->
                entry.remainingTicks = task.remainingTicks
            },
            onComplete = {
                stopTimers()
            }
        )
    }

    private fun unpauseTimer() {
        if (!World.isInSkyblock) return
        val task = serverCountdownTask
        if (task != null) {
            task.unpause()
        } else if (entry.remainingTicks > 0L) {
            serverCountdownTask = registerServerCountdownEvent(
                durationTicks = entry.remainingTicks,
                interval = 20L,
                onTick = { task ->
                    entry.remainingTicks = task.remainingTicks
                },
                onComplete = {
                    stopTimers()
                }
            )
        }
    }

    private fun pauseTimer() {
        val task = serverCountdownTask
        if (task != null) {
            task.pause()
            entry.remainingTicks = task.remainingTicks
            OtherManager.file.save()
        }
    }

    fun resyncTimer(ticks: Long) {
        if (!World.isInSkyblock) return

        entry.remainingTicks = ticks
        OtherManager.file.save()
        val currentTask = serverCountdownTask
        if (currentTask != null && !currentTask.isCompleted) {
            currentTask.resync(ticks)
        } else {
            unpauseTimer()
        }
    }

    fun stopTimers() {
        serverCountdownTask?.unregister()
        serverCountdownTask = null
        entry.remainingTicks = 0L
        entry.isCollectorsEdition = false
        OtherManager.file.save()
    }
}
