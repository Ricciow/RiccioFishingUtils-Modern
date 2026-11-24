package cloud.glitchdev.rfu.events.managers.connection

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.registration.Defaults
import cloud.glitchdev.rfu.utils.Coroutines
import kotlinx.coroutines.delay

@EventManager(register = false)
object JoinEventManager : AbstractEventManager<(wasConnected : Boolean) -> Unit, JoinEventManager.JoinEvent>() {
    internal const val DEFAULT_JOIN_DELAY_MILLIS = 0L

    override val runTasks: (Boolean) -> Unit = { wasConnected ->
        tasks.forEach { task ->
            if (task.delayMillis <= 0L) {
                safeExecution { task.callback(wasConnected) }
            } else {
                Coroutines.launch {
                    delay(task.delayMillis)
                    safeExecution {
                        if (task in tasks) task.callback(wasConnected)
                    }
                }
            }
        }
    }

    fun register(priority: Int = DEFAULT_EVENT_PRIORITY, delayMillis: Long = DEFAULT_JOIN_DELAY_MILLIS, callback: (wasConnected: Boolean) -> Unit): JoinEvent {
        return JoinEvent(priority, delayMillis, callback).register()
    }

    @Defaults("delayMillis = ${DEFAULT_JOIN_DELAY_MILLIS}L")
    class JoinEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        val delayMillis: Long = DEFAULT_JOIN_DELAY_MILLIS,
        callback: (wasConnected: Boolean) -> Unit,
    ) : ManagedTask<(wasConnected: Boolean) -> Unit, JoinEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
