package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object SeaCreaturePersonalBestEvents : AbstractEventManager<(SeaCreatures, Long) -> Unit, SeaCreaturePersonalBestEvents.SeaCreaturePersonalBestEvent>() {
    override val runTasks: (SeaCreatures, Long) -> Unit = { sc, durationMillis ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc, durationMillis)
            }
        }
    }

    fun registerSeaCreaturePersonalBestEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (SeaCreatures, durationMillis: Long) -> Unit
    ): SeaCreaturePersonalBestEvent {
        return SeaCreaturePersonalBestEvent(priority, callback).register()
    }

    class SeaCreaturePersonalBestEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (SeaCreatures, Long) -> Unit
    ) : ManagedTask<(SeaCreatures, Long) -> Unit, SeaCreaturePersonalBestEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
