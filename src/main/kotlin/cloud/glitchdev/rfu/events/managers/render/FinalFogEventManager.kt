package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.fog.FogData

@EventManager(register = false)
object FinalFogEventManager : AbstractEventManager<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FinalFogEventManager.FinalFogEvent>() {
    override val runTasks: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit = { fog, camera, level, renderDistance, deltaTracker ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(fog, camera, level, renderDistance, deltaTracker) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit): FinalFogEvent =
        FinalFogEvent(priority, callback).register()

    class FinalFogEvent(
        priority: Int,
        callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit
    ) : ManagedTask<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FinalFogEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
