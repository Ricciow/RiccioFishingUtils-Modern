package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature

@RFUFeature
object CollectionHour : Feature {
    override fun onInitialize() {
        registerTickEvent {

        }
    }
}