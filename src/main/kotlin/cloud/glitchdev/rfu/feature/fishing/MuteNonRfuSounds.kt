package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerSoundPlayEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object MuteNonRfuSounds : Feature {
    override fun onInitialize(scope: EventScope) {
        scope.registerSoundPlayEvent { sound, cancelable ->
            if (GeneralFishing.muteNonRfuSounds && sound.identifier.namespace != MOD_ID) {
                cancelable.cancel()
            }
        }
    }
}
