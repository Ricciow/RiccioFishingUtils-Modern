package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.SetTimeEvents.registerSetTimeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import java.util.ArrayDeque
import kotlin.math.min

@RFUFeature
object TpsTracker : Feature {
    private const val MAX_SAMPLES = 20
    private const val WINDOW_MS = 20_000L

    private data class TimeSample(val timestamp: Long, val gameTime: Long)

    private val samples = ArrayDeque<TimeSample>()
    private var lastGameTime: Long = -1L
    private var lastPacketTime: Long = 0L
    private var measuredTps: Double = 20.0

    override fun onInitialize() {
        registerSetTimeEvent { gameTime ->
            val now = System.currentTimeMillis()

            if (lastGameTime == -1L) {
                lastGameTime = gameTime
                lastPacketTime = now
                samples.clear()
                samples.add(TimeSample(now, gameTime))
                measuredTps = 20.0
                return@registerSetTimeEvent
            }

            val deltaTicks = gameTime - lastGameTime
            val deltaTime = now - lastPacketTime

            if (deltaTicks !in 1..2000 || deltaTime <= 0) {
                lastGameTime = gameTime
                lastPacketTime = now
                samples.clear()
                samples.add(TimeSample(now, gameTime))
                measuredTps = 20.0
                return@registerSetTimeEvent
            }

            samples.add(TimeSample(now, gameTime))
            while (samples.size > MAX_SAMPLES || (samples.size > 2 && now - samples.first().timestamp > WINDOW_MS)) {
                samples.removeFirst()
            }

            if (samples.size >= 2) {
                val first = samples.first()
                val last = samples.last()
                val totalTicks = last.gameTime - first.gameTime
                val totalTimeSec = (last.timestamp - first.timestamp) / 1000.0

                if (totalTimeSec > 0.0) {
                    measuredTps = (totalTicks / totalTimeSec).coerceIn(0.0, 20.0)
                }
            }

            lastGameTime = gameTime
            lastPacketTime = now
        }

        registerLocationEvent {
            reset()
        }

        registerDisconnectEvent {
            reset()
        }
    }

    fun reset() {
        samples.clear()
        lastGameTime = -1L
        lastPacketTime = 0L
        measuredTps = 20.0
    }

    fun getTps(): Double {
        if (lastPacketTime == 0L || samples.isEmpty()) {
            return 20.0
        }

        val now = System.currentTimeMillis()
        val timeSinceLastPacket = now - lastPacketTime

        if (timeSinceLastPacket > 30_000L) {
            return 0.0
        }

        if (timeSinceLastPacket > 1000L && samples.isNotEmpty()) {
            val first = samples.first()
            val totalTicks = samples.last().gameTime - first.gameTime
            val liveTimeSec = (now - first.timestamp) / 1000.0

            if (liveTimeSec > 0.0 && totalTicks > 0) {
                val liveTps = (totalTicks / liveTimeSec).coerceIn(0.0, 20.0)
                return min(measuredTps, liveTps)
            } else {
                val singlePacketTps = (20.0 * 1000.0 / timeSinceLastPacket).coerceIn(0.0, 20.0)
                return min(measuredTps, singlePacketTps)
            }
        }

        return measuredTps
    }
}
