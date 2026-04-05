package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.InstantRegister
import cloud.glitchdev.rfu.events.InstantRegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.getResource
import cloud.glitchdev.rfu.utils.dsl.parseResource
import com.google.gson.JsonParser
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import java.io.InputStreamReader

@InstantRegister
object Sounds : InstantRegisteredEvent {
    val registeredSounds = mutableSetOf<String>()

    override fun instantRegister() {
        val stream = Sounds::class.java.getResourceAsStream("/assets/$MOD_ID/sounds.json")
        if (stream == null) {
            RFULogger.warn("Could not find sounds.json to register sounds!")
            return
        }

        try {
            val json = JsonParser.parseReader(InputStreamReader(stream))
            if (json.isJsonObject) {
                val names = json.asJsonObject.keySet()
                names.forEach { soundName ->
                    registerSound(soundName)
                }
            }
        } catch (e: Exception) {
            RFULogger.error("Failed to automatically register sounds from sounds.json", e)
        } finally {
            stream.close()
        }
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
        registeredSounds.add(id)
        val resourceLocation = getResource(id)
        val sound = SoundEvent.createVariableRangeEvent(resourceLocation)
        Registry.register(BuiltInRegistries.SOUND_EVENT, resourceLocation, sound)
    }
}
