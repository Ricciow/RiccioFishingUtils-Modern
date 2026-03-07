package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.InstantRegister
import cloud.glitchdev.rfu.events.InstantRegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.getResource
import cloud.glitchdev.rfu.utils.dsl.parseResource
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent

@InstantRegister
object Sounds : InstantRegisteredEvent {
    override fun instantRegister() {
        registerSound("rare_sc")
        registerSound("death")
    }

    fun playSound(id: String, pitch: Float = 1f, volume: Float = 1f) {
        val resourceLocation = parseResource(id)
        if (resourceLocation == null) {
            RFULogger.warn("Invalid sound ID format: '$id'")
            return
        }

        val optionalSound = BuiltInRegistries.SOUND_EVENT.getOptional(resourceLocation)
        if (optionalSound.isEmpty) {
            RFULogger.warn("Attempted to play sound '$id' but it does not exist in the registry!")
            return
        }

        val sound = optionalSound.get()

        val soundInstance = SimpleSoundInstance.forUI(sound, pitch, volume)
        mc.soundManager.play(soundInstance)
    }

    private fun registerSound(id: String) {
        val resourceLocation = getResource(id)
        val sound = SoundEvent.createVariableRangeEvent(resourceLocation)
        Registry.register(BuiltInRegistries.SOUND_EVENT, resourceLocation, sound)
    }
}