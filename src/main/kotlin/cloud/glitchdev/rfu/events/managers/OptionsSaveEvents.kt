package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager

object OptionsSaveEvents : AbstractEventManager<(isPre: Boolean) -> Unit, OptionsSaveEvents.OptionsSaveEvent>() {
    override val runTasks: (Boolean) -> Unit = { isPre ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(isPre)
            }
        }
    }

    fun registerOptionsSaveEvent(priority: Int = 20, callback: (isPre: Boolean) -> Unit): OptionsSaveEvent {
        return OptionsSaveEvent(priority, callback).register()
    }

    fun registerBeforeOptionsSave(priority: Int = 20, callback: () -> Unit): OptionsSaveEvent {
        return registerOptionsSaveEvent(priority) { if (it) callback() }
    }

    fun registerAfterOptionsSave(priority: Int = 20, callback: () -> Unit): OptionsSaveEvent {
        return registerOptionsSaveEvent(priority) { if (!it) callback() }
    }

    class OptionsSaveEvent(
        priority: Int = 20,
        callback: (Boolean) -> Unit
    ) : ManagedTask<(Boolean) -> Unit, OptionsSaveEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
