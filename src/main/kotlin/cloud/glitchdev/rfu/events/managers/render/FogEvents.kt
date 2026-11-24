package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.fog.FogData

object FogEvents {
    /** Runs after atmospheric fog is configured. */
    fun registerFogEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit
    ): FogEventManager.FogEvent = FogEventManager.register(priority, callback)

    /** Runs after all fog settings are finalized, for every environment. */
    fun registerFinalFogEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit
    ): FinalFogEventManager.FinalFogEvent = FinalFogEventManager.register(priority, callback)

    fun registerCameraFluidEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (CameraFluidEventManager.CameraFluidContext) -> Unit
    ): CameraFluidEventManager.CameraFluidEvent = CameraFluidEventManager.register(priority, callback)

    fun registerFogColorEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (FogColorEventManager.FogColorContext) -> Unit
    ): FogColorEventManager.FogColorEvent = FogColorEventManager.register(priority, callback)

    fun registerWaterVisionFluidEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (WaterVisionFluidEventManager.WaterVisionFluidContext) -> Unit
    ): WaterVisionFluidEventManager.WaterVisionFluidEvent = WaterVisionFluidEventManager.register(priority, callback)
}
