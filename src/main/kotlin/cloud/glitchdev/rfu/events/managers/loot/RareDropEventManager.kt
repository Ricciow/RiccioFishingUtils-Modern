package cloud.glitchdev.rfu.events.managers.loot

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object RareDropEventManager : AbstractEventManager<(rareDrop: RareDrops, magicFind: Int?, mobName: String?) -> Boolean, RareDropEventManager.RareDropEvent>() {
    override val runTasks: (RareDrops, Int?, String?) -> Boolean = { rareDrop, magicFind, mobName ->
        var result = true
        safeExecution(mainThread = false) {
            tasks.forEach { event -> if (!event.callback(rareDrop, magicFind, mobName)) result = false }
        }
        result
    }

    fun register(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (rareDrop: RareDrops, magicFind: Int?, mobName: String?) -> Boolean
    ): RareDropEvent {
        return RareDropEvent(priority, callback).register()
    }

    class RareDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (rareDrop: RareDrops, magicFind: Int?, mobName: String?) -> Boolean
    ) : ManagedTask<(rareDrop: RareDrops, magicFind: Int?, mobName: String?) -> Boolean, RareDropEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
