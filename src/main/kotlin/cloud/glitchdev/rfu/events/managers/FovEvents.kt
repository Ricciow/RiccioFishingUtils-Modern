package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.wrappers.Cancelable

@EventManager(register = false)
object FovEvents : AbstractEventManager<(Cancelable<Float>, Float) -> Unit, FovEvents.FovEvent>() {
    override val runTasks: (Cancelable<Float>, Float) -> Unit = { cancelable, partialTick ->
        safeExecution {
            for (task in tasks) {
                task.callback(cancelable, partialTick)
            }
        }
    }

    fun registerFovEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Cancelable<Float>, Float) -> Unit): FovEvent {
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