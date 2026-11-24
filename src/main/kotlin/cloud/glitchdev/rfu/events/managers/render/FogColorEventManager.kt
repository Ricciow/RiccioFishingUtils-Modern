package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import java.awt.Color
import net.minecraft.client.Camera
import net.minecraft.client.renderer.fog.environment.FogEnvironment

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
