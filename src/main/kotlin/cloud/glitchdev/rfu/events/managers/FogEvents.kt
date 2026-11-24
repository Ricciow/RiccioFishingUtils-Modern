package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.fog.FogData
import net.minecraft.client.renderer.fog.environment.FogEnvironment
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import java.awt.Color

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

    @EventManager(register = false)
    object FogEventManager : AbstractEventManager<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FogEventManager.FogEvent>() {
        override val runTasks: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit = { fog, camera, level, renderDistance, deltaTracker ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(fog, camera, level, renderDistance, deltaTracker) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit): FogEvent =
            FogEvent(priority, callback).register()

        class FogEvent(
            priority: Int,
            callback: (FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit
        ) : ManagedTask<(FogData, Camera, ClientLevel, Float, DeltaTracker) -> Unit, FogEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

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

    @EventManager(register = false)
    object CameraFluidEventManager : AbstractEventManager<(CameraFluidEventManager.CameraFluidContext) -> Unit, CameraFluidEventManager.CameraFluidEvent>() {
        override val runTasks: (CameraFluidContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (CameraFluidContext) -> Unit): CameraFluidEvent =
            CameraFluidEvent(priority, callback).register()

        class CameraFluidContext(val camera: Camera, val state: FluidState, val tag: TagKey<Fluid>, var matches: Boolean) {
            var disableFog: Boolean = false
        }

        class CameraFluidEvent(priority: Int, callback: (CameraFluidContext) -> Unit) :
            ManagedTask<(CameraFluidContext) -> Unit, CameraFluidEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object WaterVisionFluidEventManager : AbstractEventManager<(WaterVisionFluidEventManager.WaterVisionFluidContext) -> Unit, WaterVisionFluidEventManager.WaterVisionFluidEvent>() {
        override val runTasks: (WaterVisionFluidContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (WaterVisionFluidContext) -> Unit): WaterVisionFluidEvent =
            WaterVisionFluidEvent(priority, callback).register()

        class WaterVisionFluidContext(val player: LocalPlayer, var submerged: Boolean)

        class WaterVisionFluidEvent(priority: Int, callback: (WaterVisionFluidContext) -> Unit) :
            ManagedTask<(WaterVisionFluidContext) -> Unit, WaterVisionFluidEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    @EventManager(register = false)
    object FogColorEventManager : AbstractEventManager<(FogColorEventManager.FogColorContext) -> Unit, FogColorEventManager.FogColorEvent>() {
        override val runTasks: (FogColorContext) -> Unit = { context ->
            safeExecution(mainThread = false) {
                tasks.forEach { it.callback(context) }
            }
        }

        fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FogColorContext) -> Unit): FogColorEvent =
            FogColorEvent(priority, callback).register()

        class FogColorContext(val camera: Camera, val environment: FogEnvironment) {
            var color: Color? = null
            var preserveBrightness: Boolean = false
        }

        class FogColorEvent(priority: Int, callback: (FogColorContext) -> Unit) :
            ManagedTask<(FogColorContext) -> Unit, FogColorEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
