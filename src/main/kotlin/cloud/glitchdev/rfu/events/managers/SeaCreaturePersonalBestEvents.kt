package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager

object SeaCreaturePersonalBestEvents : AbstractEventManager<(SeaCreatures, Long) -> Unit, SeaCreaturePersonalBestEvents.SeaCreaturePersonalBestEvent>() {
    override val runTasks: (SeaCreatures, Long) -> Unit = { sc, durationMillis ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc, durationMillis)
            }
        }
    }

    fun registerSeaCreaturePersonalBestEvent(
        priority: Int = 20,
        callback: (SeaCreatures, durationMillis: Long) -> Unit
    ): SeaCreaturePersonalBestEvent {
        return SeaCreaturePersonalBestEvent(priority, callback).register()
    }

    class SeaCreaturePersonalBestEvent(
        priority: Int = 20,
        callback: (SeaCreatures, Long) -> Unit
    ) : ManagedTask<(SeaCreatures, Long) -> Unit, SeaCreaturePersonalBestEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
