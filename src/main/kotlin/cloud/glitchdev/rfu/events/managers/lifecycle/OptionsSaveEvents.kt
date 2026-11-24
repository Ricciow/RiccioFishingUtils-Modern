package cloud.glitchdev.rfu.events.managers.lifecycle

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.registration.ScopedRegistration

@EventManager(register = false)
object OptionsSaveEvents : AbstractEventManager<(isPre: Boolean) -> Unit, OptionsSaveEvents.OptionsSaveEvent>() {
    override val runTasks: (Boolean) -> Unit = { isPre ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(isPre)
            }
        }
    }

    @ScopedRegistration
    internal fun registerBeforeOptionsSave(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): OptionsSaveEvent {
        return OptionsSaveEvent(priority) { if (it) callback() }.register()
    }

    @ScopedRegistration
    internal fun registerAfterOptionsSave(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): OptionsSaveEvent {
        return OptionsSaveEvent(priority) { if (!it) callback() }.register()
    }

    class OptionsSaveEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Boolean) -> Unit
    ) : ManagedTask<(Boolean) -> Unit, OptionsSaveEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
