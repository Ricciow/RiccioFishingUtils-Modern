package cloud.glitchdev.rfu.events.managers.loot

import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager

@EventManager(register = false)
object DyeDropEventManager : AbstractEventManager<(dyeDrop: Dyes, magicFind : Int?, mobName: String?) -> Unit, DyeDropEventManager.DyeDropEvent>() {
    override val runTasks: (Dyes, Int?, String?) -> Unit = { dyeDrop, magicFind, mobName ->
        safeExecution(mainThread = false) {
            tasks.forEach { event -> event.callback(dyeDrop, magicFind, mobName) }
        }
    }

    class DyeDropEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (dyeDrop: Dyes, magicFind : Int?, mobName: String?) -> Unit
    ) : ManagedTask<(dyeDrop: Dyes, magicFind : Int?, mobName: String?) -> Unit, DyeDropEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
