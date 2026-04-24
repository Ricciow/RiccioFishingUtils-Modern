package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SlidingRateTracker(
    private val limitProvider: () -> Duration = { GeneralFishing.fishingTime.minutes }
) {
    private val history = ArrayDeque<Pair<Instant, Double>>()
    var total: Double = 0.0
        private set
    var currentRatePerHour: Double = 0.0
        private set
    val overallRatePerHour: Double
        get() {
            val hoursElapsed = FishingSession.duration.inWholeMilliseconds / 3600000.0
            return if (hoursElapsed > 0) total / hoursElapsed else 0.0
        }
    var lastEvent: Instant = Instant.DISTANT_PAST
        private set

    fun addEvent(value: Double) {
        val now = Clock.System.now()
        lastEvent = now
        total += value
        history.add(Pair(now, value))
        update()
    }

    fun update() {
        val now = Clock.System.now()
        val limit = limitProvider()

        val isDowntimeReached = lastEvent != Instant.DISTANT_PAST && (now - lastEvent) > limit

        if (isDowntimeReached) {
            if (!GeneralFishing.pauseSessionOnWindowReached) {
                reset()
                return
            }
        }

        if (!FishingSession.isFishing) {
            currentRatePerHour = 0.0
            return
        }

        if (FishingSession.isPaused || (isDowntimeReached && GeneralFishing.pauseSessionOnWindowReached)) return

        while (history.isNotEmpty() && (now - history.first().first) > limit) {
            history.removeFirst()
        }

        val timeElapsed = FishingSession.duration
        val calculationWindow = if (timeElapsed < limit) timeElapsed else limit

        if (calculationWindow.inWholeSeconds == 0L) {
            currentRatePerHour = 0.0
            return
        }

        val windowSum = history.sumOf { it.second }
        currentRatePerHour = (windowSum / calculationWindow.inWholeSeconds) * 3600
    }

    fun shiftHistory(duration: Duration) {
        val newHistory = history.map { (instant, value) -> Pair(instant + duration, value) }
        history.clear()
        history.addAll(newHistory)
        if (lastEvent != Instant.DISTANT_PAST) lastEvent += duration
    }

    fun reset() {
        lastEvent = Instant.DISTANT_PAST
        history.clear()
        total = 0.0
        currentRatePerHour = 0.0
    }
}
