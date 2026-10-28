package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@RFUFeature
object JawbusDeathAlert : Feature {
    val JAWBUS_DEATH_REGEX = """☠ ([\w_]{3,16}) was killed by Lord Jawbus\.""".toRegex()

    private var lastAlert : Instant = Instant.DISTANT_PAST

    override fun onInitialize() {
        registerGameEvent(JAWBUS_DEATH_REGEX) { _, _, matches ->
            if(!LavaFishing.jawbusDeath.enabled) return@registerGameEvent
            val now = Clock.System.now()
            if(now - lastAlert > 30.seconds) {
                val username = matches?.groupValues?.getOrNull(1) ?: return@registerGameEvent
                LavaFishing.jawbusDeath.show("player" to username)
                lastAlert = now
                if (LavaFishing.jawbusDeath.sound) {
                    Sounds.playSound("rfu:jawbus_death", 1f, LavaFishing.jawbusDeath.volume)
                }
            }
        }
    }
}
