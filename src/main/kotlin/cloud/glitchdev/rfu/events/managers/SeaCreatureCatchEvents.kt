package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.phys.Vec3

import cloud.glitchdev.rfu.data.fishing.Hotspot

@AutoRegister
object SeaCreatureCatchEvents : AbstractEventManager<(SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3) -> Unit, SeaCreatureCatchEvents.SeaCreatureCatchEvent>(), RegisteredEvent {
    val SC_MESSAGE_REGEX = SeaCreatures.entries.joinToString("|") { it.catchMessage.escapeForRegex() }.toExactRegex()
    val DOUBLE_HOOK_REGEX = """Double Hook!|It's a Double Hook! Woot woot!|It's a Double Hook!""".toExactRegex()
    var isDoubleHook = false

    override fun register() {
        registerGameEvent(DOUBLE_HOOK_REGEX) { _, _, _ ->
            isDoubleHook = true
        }

        registerGameEvent(SC_MESSAGE_REGEX) { message, _, _ ->
            val catchMessage = message.toUnformattedString()
            val sc = SeaCreatures.entries.find { it.catchMessage == catchMessage }
            if(sc != null) {
                val bobber = mc.player?.fishing
                val checkPos = bobber?.position() ?: mc.player?.position() ?: Vec3.ZERO
                val hotspot = HotSpotEvents.getHotspotAt(checkPos)
                runTasks(sc, isDoubleHook, hotspot, checkPos)
            }
            isDoubleHook = false
        }
    }

    override val runTasks: (SeaCreatures, Boolean, Hotspot?, Vec3) -> Unit = { sc, doubleHook, hotspot, pos ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc, doubleHook, hotspot, pos)
            }
        }
    }

    fun registerSeaCreatureCatchEvent(priority: Int = 20, callback: (SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3) -> Unit): SeaCreatureCatchEvent {
        return SeaCreatureCatchEvent(priority, callback).register()
    }

    class SeaCreatureCatchEvent(
        priority: Int = 20,
        callback: (SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3) -> Unit
    ) : ManagedTask<(SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3) -> Unit, SeaCreatureCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}