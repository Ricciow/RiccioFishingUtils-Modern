package cloud.glitchdev.rfu.events.managers.input

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object RawInputEventManager : AbstractEventManager<(Int, Int, Int) -> Unit, RawInputEventManager.RawInputEvent>() {
    override val runTasks: (Int, Int, Int) -> Unit = { code, action, modifiers ->
        safeExecution {
            tasks.forEach { task -> task.callback(code, action, modifiers) }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (Int, Int, Int) -> Unit): RawInputEvent {
        return RawInputEvent(priority, callback).register()
    }

    class RawInputEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Int, Int, Int) -> Unit
    ) : ManagedTask<(Int, Int, Int) -> Unit, RawInputEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
