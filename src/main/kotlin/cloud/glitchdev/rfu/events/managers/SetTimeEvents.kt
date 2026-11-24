package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object SetTimeEvents : AbstractEventManager<(gameTime: Long) -> Unit, SetTimeEvents.SetTimeEvent>() {
    override val runTasks: (Long) -> Unit = { gameTime ->
        safeExecution {
            tasks.forEach { event -> event.callback(gameTime) }
        }
    }

    fun registerSetTimeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (gameTime: Long) -> Unit
    ): SetTimeEvent {
        return SetTimeEvent(priority, callback).register()
    }

    class SetTimeEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (gameTime: Long) -> Unit
    ) : ManagedTask<(Long) -> Unit, SetTimeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
