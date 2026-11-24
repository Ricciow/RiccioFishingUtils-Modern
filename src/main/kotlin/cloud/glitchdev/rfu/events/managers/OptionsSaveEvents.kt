package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.OptionsSaveEvents.OptionsSaveEvent

@EventManager(register = false)
object OptionsSaveEvents : AbstractEventManager<(isPre: Boolean) -> Unit, OptionsSaveEvents.OptionsSaveEvent>() {
    override val runTasks: (Boolean) -> Unit = { isPre ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(isPre)
            }
        }
    }

    fun registerOptionsSaveEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (isPre: Boolean) -> Unit): OptionsSaveEvent {
        return OptionsSaveEvent(priority, callback).register()
    }

    fun registerBeforeOptionsSave(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): OptionsSaveEvent {
        return registerOptionsSaveEvent(priority) { if (it) callback() }
    }

    fun registerAfterOptionsSave(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): OptionsSaveEvent {
        return registerOptionsSaveEvent(priority) { if (!it) callback() }
    }

    class OptionsSaveEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (Boolean) -> Unit
    ) : ManagedTask<(Boolean) -> Unit, OptionsSaveEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}

fun EventScope.registerBeforeOptionsSave(
    priority: Int = DEFAULT_EVENT_PRIORITY,
    callback: () -> Unit,
): OptionsSaveEvent = registerTask { OptionsSaveEvents.registerBeforeOptionsSave(priority, callback) }

fun EventScope.registerAfterOptionsSave(
    priority: Int = DEFAULT_EVENT_PRIORITY,
    callback: () -> Unit,
): OptionsSaveEvent = registerTask { OptionsSaveEvents.registerAfterOptionsSave(priority, callback) }
