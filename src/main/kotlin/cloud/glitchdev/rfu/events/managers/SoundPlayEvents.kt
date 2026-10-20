package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundEngine

object SoundPlayEvents : AbstractEventManager<(SoundInstance, Cancelable<SoundEngine.PlayResult>) -> Unit, SoundPlayEvents.SoundPlayEvent>() {
    override val runTasks: (SoundInstance, Cancelable<SoundEngine.PlayResult>) -> Unit = { sound, cancelable ->
        // Cancellation must finish before the sound engine continues playback.
        safeExecution(mainThread = false) {
            tasks.forEach { task -> task.callback(sound, cancelable) }
        }
    }

    fun registerSoundPlayEvent(
        priority: Int = 20,
        callback: (SoundInstance, Cancelable<SoundEngine.PlayResult>) -> Unit
    ): SoundPlayEvent {
        return SoundPlayEvent(priority, callback).register()
    }

    class SoundPlayEvent(
        priority: Int = 20,
        callback: (SoundInstance, Cancelable<SoundEngine.PlayResult>) -> Unit
    ) : ManagedTask<(SoundInstance, Cancelable<SoundEngine.PlayResult>) -> Unit, SoundPlayEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
