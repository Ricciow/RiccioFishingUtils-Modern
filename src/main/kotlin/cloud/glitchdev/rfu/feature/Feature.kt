package cloud.glitchdev.rfu.feature

import cloud.glitchdev.rfu.events.EventScope

interface Feature {
    fun onInitialize(scope: EventScope)
}