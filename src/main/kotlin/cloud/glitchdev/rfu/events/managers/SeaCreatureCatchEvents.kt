package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import gg.essential.universal.utils.toUnformattedString

@AutoRegister
object SeaCreatureCatchEvents : AbstractEventManager<(SeaCreatures) -> Unit, SeaCreatureCatchEvents.SeaCreatureCatchEvent>(), RegisteredEvent {
    val SC_MESSAGE_REGEX = SeaCreatures.entries.joinToString("|") { it.catchMessage }.toRegex()

    override fun register() {
        registerGameEvent(SC_MESSAGE_REGEX) { message, _, _ ->
            val catchMessage = message.toUnformattedString()
            val sc = SeaCreatures.entries.find { it.catchMessage == catchMessage }
            if(sc != null) {
                runTasks(sc)
            }
        }
    }

    fun runTasks(sc : SeaCreatures) {
        tasks.forEach { task ->
            task.callback(sc)
        }
    }

    fun registerSeaCreatureCatchEvent(priority: Int = 20, callback: (SeaCreatures) -> Unit): SeaCreatureCatchEvent {
        return SeaCreatureCatchEvent(priority, callback).register()
    }

    class SeaCreatureCatchEvent(
        priority: Int = 20,
        callback: (SeaCreatures) -> Unit
    ) : ManagedTask<(SeaCreatures) -> Unit, SeaCreatureCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}