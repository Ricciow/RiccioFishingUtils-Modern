package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.GeneralFishing.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.catches.CatchTracker
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@RFUFeature
object RareScPartyMessage : Feature {
    override fun onInitialize() {
        registerSeaCreatureCatchEvent(-10) { seaCreature ->
            if(!GeneralFishing.rarePartyMessages) return@registerSeaCreatureCatchEvent

            if(RARE_SC_REGEX.matches(seaCreature.scName)) {
                val history = CatchTracker.catchHistory.getOrAdd(seaCreature)
                val timeSinceLast = if (history.total > 0) {
                    (Clock.System.now() - history.time).toReadableString()
                } else {
                    "First Catch!"
                }

                val messageString = GeneralFishing.rarePartyMessage
                    .replace("{name}", seaCreature.scName)
                    .replace("{count}", (history.total + 1).toString())
                    .replace("{time}", timeSinceLast)

                Chat.sendPartyMessage(messageString)
            }
        }
    }
}