package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import gg.essential.universal.utils.toUnformattedString

@AutoRegister
object SeaCreatureCatchEvents : AbstractEventManager<(SeaCreatures, doubleHook : Boolean) -> Unit, SeaCreatureCatchEvents.SeaCreatureCatchEvent>(), RegisteredEvent {
    val SC_MESSAGE_REGEX = SeaCreatures.entries.joinToString("|") { it.catchMessage }.toRegex()
    val DOUBLE_HOOK_REGEX = """Double Hook!|It's a Double Hook! Woot woot!|It's a Double Hook!""".toRegex()
    var isDoubleHook = false

    override fun register() {
        registerGameEvent(DOUBLE_HOOK_REGEX) { _, _, _ ->
            isDoubleHook = true
        }

        registerGameEvent(SC_MESSAGE_REGEX) { message, _, _ ->
            val catchMessage = message.toUnformattedString()
            val sc = SeaCreatures.entries.find { it.catchMessage == catchMessage }
            if(sc != null) {
                runTasks(sc)
            }
            isDoubleHook = false
        }
    }

    fun runTasks(sc : SeaCreatures) {
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc, isDoubleHook)
            }
        }
    }

    fun registerSeaCreatureCatchEvent(priority: Int = 20, callback: (SeaCreatures, doubleHook : Boolean) -> Unit): SeaCreatureCatchEvent {
        return SeaCreatureCatchEvent(priority, callback).register()
    }

    class SeaCreatureCatchEvent(
        priority: Int = 20,
        callback: (SeaCreatures, doubleHook : Boolean) -> Unit
    ) : ManagedTask<(SeaCreatures, doubleHook : Boolean) -> Unit, SeaCreatureCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}