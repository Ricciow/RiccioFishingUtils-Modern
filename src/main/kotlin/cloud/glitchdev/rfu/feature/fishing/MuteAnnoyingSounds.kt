package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerSoundPlayEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import net.minecraft.sounds.SoundEvents

@RFUFeature
object MuteAnnoyingSounds : Feature {
    override fun onInitialize(scope: EventScope) {
        scope.registerSoundPlayEvent { sound, cancelable ->
            if (!GeneralFishing.muteAnnoyingSounds) return@registerSoundPlayEvent

            val mute = when (World.island) {
                FishingIslands.BAYOU -> sound.identifier == SoundEvents.GHAST_AMBIENT.location()
                FishingIslands.ISLE -> sound.identifier == SoundEvents.ELDER_GUARDIAN_AMBIENT.location()
                    || sound.identifier == SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND.location()
                FishingIslands.JERRY -> sound.identifier == SoundEvents.TOTEM_USE.location()
                FishingIslands.MOONGLADE_MARSH -> sound.identifier == SoundEvents.ENDER_DRAGON_GROWL.location()
                    || sound.identifier == SoundEvents.ENDER_DRAGON_FLAP.location()
                else -> false
            }
            if (mute) cancelable.cancel()
        }
    }
}
