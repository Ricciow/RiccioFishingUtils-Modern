package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.fog.FogData

object FogEvents : AbstractEventManager<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FogEvents.FogEvent>() {
    override val runTasks: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit = { fog, camera, level, renderDistance, deltaTracker ->
        safeExecution {
            for (task in tasks) {
                task.callback(fog, camera, level, renderDistance, deltaTracker)
            }
        }
    }

    fun registerFogEvent(priority: Int = 20, callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit): FogEvent {
        return FogEvent(priority, callback).register()
    }

    class FogEvent(
        priority: Int,
        callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit
    ) : ManagedTask<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FogEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
