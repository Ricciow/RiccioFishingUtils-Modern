package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.MobyDuckEntry
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerAnyChatEvent
import cloud.glitchdev.rfu.events.managers.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.ServerCountdownEvents.ServerCountdownEvent
import cloud.glitchdev.rfu.events.managers.registerServerCountdownEvent
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

    private var countdownScope: EventScope? = null
    private var serverCountdownTask: ServerCountdownEvent? = null

    val remainingDuration: Duration
        get() = serverCountdownTask?.remainingDuration ?: Duration.ZERO

    val isActive: Boolean
        get() = (serverCountdownTask?.remainingTicks ?: entry.remainingTicks) > 0L && World.isInSkyblock

    internal val mobyDuckRegex = """You consumed a Moby-Duck(?::\s*(Collector's Edition))?\s*and gained \+([\d.]+)[\s☯]*Fishing Wisdom for 60m!""".toExactRegex()
    private val expirationWarningRegex = """Moby-Duck expires in (\d+)s!?""".toExactRegex()
    private val expiredRegex = """Moby-Duck has expired!?""".toExactRegex()

    override fun onInitialize(scope: EventScope) {
        scope.onClose(::closeCountdown)

        scope.registerAnyChatEvent(mobyDuckRegex) { _, match ->
            val isCollectors = match?.groupValues?.getOrNull(1)?.isNotEmpty() == true
            val wisdom = match?.groupValues?.getOrNull(2) ?: "1"
            startTimer(scope, isCollectors, wisdom)
        }

        scope.registerAnyChatEvent(expirationWarningRegex) { _, match ->
            val seconds = match?.groupValues?.getOrNull(1)?.toLongOrNull() ?: return@registerAnyChatEvent
            resyncTimer(scope, seconds * 20L)
        }

        scope.registerAnyChatEvent(expiredRegex) { _, _ ->
            stopTimers()
        }

        scope.registerLocationEvent {
            if (World.isInSkyblock) {
                unpauseTimer(scope)
            } else {
                pauseTimer()
            }
        }

        scope.registerDisconnectEvent {
            pauseTimer()
        }

        if (World.isInSkyblock && entry.remainingTicks > 0L) {
            unpauseTimer(scope)
        }
    }

    private fun startTimer(scope: EventScope, isCollectors: Boolean, wisdom: String) {
        if (!World.isInSkyblock) return

        entry.isCollectorsEdition = isCollectors
        entry.wisdomValue = wisdom
        entry.remainingTicks = MOBY_DURATION
        OtherManager.file.save()

        startCountdown(scope, MOBY_DURATION)
    }

    private fun startCountdown(scope: EventScope, durationTicks: Long) {
        closeCountdown()
        val countdown = scope.child()
        countdownScope = countdown
        serverCountdownTask = countdown.registerServerCountdownEvent(
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

    private fun unpauseTimer(scope: EventScope) {
        if (!World.isInSkyblock) return
        val task = serverCountdownTask
        if (task != null) {
            task.unpause()
        } else if (entry.remainingTicks > 0L) {
            startCountdown(scope, entry.remainingTicks)
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

    private fun resyncTimer(scope: EventScope, ticks: Long) {
        if (!World.isInSkyblock) return

        entry.remainingTicks = ticks
        OtherManager.file.save()
        val currentTask = serverCountdownTask
        if (currentTask != null && !currentTask.isCompleted) {
            currentTask.resync(ticks)
        } else {
            unpauseTimer(scope)
        }
    }

    private fun closeCountdown() {
        val scope = countdownScope
        countdownScope = null
        serverCountdownTask = null
        scope?.close()
    }

    private fun stopTimers() {
        closeCountdown()
        entry.remainingTicks = 0L
        entry.isCollectorsEdition = false
        OtherManager.file.save()
    }
}
