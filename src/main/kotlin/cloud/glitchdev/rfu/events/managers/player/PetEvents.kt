package cloud.glitchdev.rfu.events.managers.player

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY

object PetEvents {
    fun registerPetUpdateEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (pet: String?, petName: String?, petLevel: Int?) -> Unit
    ): PetUpdateEventManager.PetUpdateEvent {
        return PetUpdateEventManager.register(priority, callback)
    }
}
