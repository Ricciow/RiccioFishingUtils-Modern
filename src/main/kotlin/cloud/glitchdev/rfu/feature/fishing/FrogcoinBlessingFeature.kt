package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.FrogcoinBlessingsEntry
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlin.time.Duration.Companion.minutes

@RFUFeature
object FrogcoinBlessingFeature : Feature {
    private val entry: FrogcoinBlessingsEntry
        get() = OtherManager.getField("frogcoin_blessings") { FrogcoinBlessingsEntry() } as? FrogcoinBlessingsEntry ?: FrogcoinBlessingsEntry()

    val activeBlessings: Map<String, Long>
        get() {
            val now = System.currentTimeMillis()
            return entry.activeBlessings.filterValues { it > now }
        }

    private val blessingRegex = """WISE! You've been granted \+(.+?) for (\d+)m while on the Lotus Atoll!""".toExactRegex()

    override fun onInitialize() {
        registerGameEvent(filter = blessingRegex) { _, _, match ->
            val matchResult = match ?: return@registerGameEvent
            val buffInfo = matchResult.groupValues[1]
            val durationMinutes = matchResult.groupValues[2].toLongOrNull() ?: 30L

            val expireTime = System.currentTimeMillis() + durationMinutes.minutes.inWholeMilliseconds
            entry.activeBlessings[buffInfo] = expireTime
            OtherManager.file.save()
        }

        registerTickEvent(interval = 20) {
            val now = System.currentTimeMillis()
            var changed = false
            val it = entry.activeBlessings.entries.iterator()
            while (it.hasNext()) {
                val next = it.next()
                if (now >= next.value) {
                    it.remove()
                    changed = true
                }
            }
            if (changed) {
                OtherManager.file.save()
            }
        }
    }
}
