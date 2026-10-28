package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import java.util.ArrayDeque

@RFUFeature
object TpsTracker : Feature {
    private const val WINDOW_NS = 5_000_000_000L
    private const val SECOND_NS = 1_000_000_000L

    private data class NetworkTimeSample(val nanoTime: Long, val gameTime: Long)

    private var lastPingId: Int? = null
    private var lastPingNanos: Long? = null
    private val networkTimeSamples = ArrayDeque<NetworkTimeSample>()

    override fun onInitialize() {
        registerLocationEvent {
            reset()
        }

        registerDisconnectEvent {
            reset()
        }
    }

    @Synchronized
    fun reset() {
        lastPingId = null
        lastPingNanos = null
        networkTimeSamples.clear()
    }

    @Synchronized
    fun onPing(id: Int) {
        if (lastPingId == id) return
        lastPingId = id

        lastPingNanos = System.nanoTime()
    }

    @Synchronized
    fun onTimePacket(gameTime: Long) {
        val now = System.nanoTime()
        networkTimeSamples.peekLast()?.let { last ->
            if (gameTime == last.gameTime) return
            if (gameTime - last.gameTime !in 1..2000) networkTimeSamples.clear()
            else if (now <= last.nanoTime) return
        }
        networkTimeSamples.addLast(NetworkTimeSample(now, gameTime))
        // Keep one sample at/before the window boundary to include interrupted intervals.
        while (networkTimeSamples.size > 100 ||
            (networkTimeSamples.size > 2 && now - networkTimeSamples.elementAt(1).nanoTime >= WINDOW_NS)
        ) {
            networkTimeSamples.removeFirst()
        }
    }

    @Synchronized
    fun getTps(): Double? {
        if (networkTimeSamples.size < 2) return null
        val first = networkTimeSamples.first()
        val last = networkTimeSamples.last()
        val span = last.nanoTime - first.nanoTime
        if (span <= 0L) return null

        val now = System.nanoTime()
        val timeSilence = now - last.nanoTime
        val pingSilence = lastPingNanos?.let { now - it } ?: Long.MAX_VALUE
        // Allow two normal time-update intervals, including on servers running below 20 TPS.
        val expectedInterval = maxOf(SECOND_NS, span / (networkTimeSamples.size - 1))
        if (timeSilence >= expectedInterval * 2 && pingSilence >= SECOND_NS) return 0.0
        // Pings alone cannot supply a tick count when time updates go missing.
        if (timeSilence >= maxOf(WINDOW_NS, expectedInterval * 3)) return null

        return ((last.gameTime - first.gameTime) * SECOND_NS.toDouble() / span).coerceIn(0.0, 20.0)
    }
}
