package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable

object FovEvents : AbstractEventManager<(Cancelable<Float>, Float) -> Unit, FovEvents.FovEvent>() {
    override val runTasks: (Cancelable<Float>, Float) -> Unit = { cancelable, partialTick ->
        safeExecution {
            for (task in tasks) {
                task.callback(cancelable, partialTick)
            }
        }
    }

    fun registerFovEvent(priority: Int = 20, callback: (Cancelable<Float>, Float) -> Unit): FovEvent {
        return FovEvent(priority, callback).register()
    }

    class FovEvent(
        priority: Int,
        callback: (Cancelable<Float>, Float) -> Unit
    ) : ManagedTask<(Cancelable<Float>, Float) -> Unit, FovEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}