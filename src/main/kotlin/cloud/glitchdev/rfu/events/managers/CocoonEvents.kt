package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import net.minecraft.network.chat.Component

@AutoRegister
object CocoonEvents : AbstractEventManager<(SeaCreatures) -> Unit, CocoonEvents.CocoonEvent>(), RegisteredEvent {
    val COCOON_REGEX = """CAUGHT! You cocooned (?:an? )?(.+?)!""".toExactRegex()

    override fun register() {
        registerGameEvent(COCOON_REGEX) { _, _, matches ->
            val scDisplayName = matches?.groupValues?.getOrNull(1)
            if (scDisplayName != null) {
                val sc = SeaCreatures.entries.find { it.scDisplayName.equals(scDisplayName, ignoreCase = true) }
                if (sc != null) {
                    runTasks(sc)
                }
            }
        }
    }

    override val runTasks: (SeaCreatures) -> Unit = { sc ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc)
            }
        }
    }

    fun registerCocoonEvent(priority: Int = 20, callback: (SeaCreatures) -> Unit): CocoonEvent {
        return CocoonEvent(priority, callback).register()
    }

    class CocoonEvent(
        priority: Int = 20,
        callback: (SeaCreatures) -> Unit
    ) : ManagedTask<(SeaCreatures) -> Unit, CocoonEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
