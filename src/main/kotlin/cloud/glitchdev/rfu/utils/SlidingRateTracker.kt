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
    private var trackingStart: Instant = Instant.DISTANT_PAST

    fun addEvent(value: Double) {
        val now = Clock.System.now()
        if (trackingStart == Instant.DISTANT_PAST) trackingStart = now
        total += value
        history.add(Pair(now, value))
        update()
    }

    fun update() {
        if (!FishingSession.isFishing) {
            currentRatePerHour = 0.0
            return
        }

        val now = Clock.System.now()
        val limit = limitProvider()
        while (history.isNotEmpty() && (now - history.first().first) > limit) {
            history.removeFirst()
        }

        val wallClockElapsed = if (trackingStart == Instant.DISTANT_PAST) Duration.ZERO else now - trackingStart
        val calculationWindow = if (wallClockElapsed < limit) wallClockElapsed else limit

        if (calculationWindow.inWholeSeconds == 0L) {
            currentRatePerHour = 0.0
            return
        }

        val windowSum = history.sumOf { it.second }
        currentRatePerHour = (windowSum / calculationWindow.inWholeSeconds) * 3600
    }

    fun reset() {
        trackingStart = Instant.DISTANT_PAST
        history.clear()
        total = 0.0
        currentRatePerHour = 0.0
    }
}
