package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.events.managers.ChatEvents.registerChatEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import kotlin.time.Duration.Companion.minutes

@RFUFeature
object FrogcoinBlessingFeature : Feature {
    private val _activeBlessings = mutableMapOf<String, Long>()
    val activeBlessings: Map<String, Long>
        get() = _activeBlessings

    private val blessingRegex = Regex("""^WISE! You've been granted \+(.+?) for (\d+)m while on the Lotus Atoll!$""")

    override fun onInitialize() {
        registerChatEvent(filter = blessingRegex) { _, match ->
            val matchResult = match ?: return@registerChatEvent
            val buffInfo = matchResult.groupValues[1]
            val durationMinutes = matchResult.groupValues[2].toLongOrNull() ?: 30L

            val expireTime = System.currentTimeMillis() + durationMinutes.minutes.inWholeMilliseconds
            _activeBlessings[buffInfo] = expireTime
        }

        registerTickEvent(interval = 20) {
            val now = System.currentTimeMillis()
            val it = _activeBlessings.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (now >= entry.value) {
                    it.remove()
                }
            }
        }
    }
}
