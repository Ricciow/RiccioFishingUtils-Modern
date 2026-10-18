package cloud.glitchdev.rfu.utils

import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class BoostedSoundInstance(sound: SoundEvent, pitch: Float, volume: Float) : SimpleSoundInstance(
    sound.location(), SoundSource.UI, volume.coerceAtMost(1f), pitch,
    SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE,
    0.0, 0.0, 0.0, true
) {
    val volumeBoost = volume.coerceAtLeast(1f)
}
