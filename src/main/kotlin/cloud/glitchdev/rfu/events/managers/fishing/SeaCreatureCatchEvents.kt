package cloud.glitchdev.rfu.events.managers.fishing

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.chat.registerGameEvent
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.fishing.Bait
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.phys.Vec3

@EventManager
object SeaCreatureCatchEvents : AbstractEventManager<(SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3, bait : Bait?) -> Unit, SeaCreatureCatchEvents.SeaCreatureCatchEvent>(), RegisteredEvent {
    val DOUBLE_HOOK_REGEX = """Double Hook!|It's a Double Hook! Woot woot!|It's a Double Hook!""".toExactRegex()
    var isDoubleHook = false

    override fun register(scope: EventScope) {
        scope.registerGameEvent(DOUBLE_HOOK_REGEX) { _, _, _ ->
            isDoubleHook = true
        }

        scope.registerGameEvent { message, _, _ ->
            val sc = SeaCreatures.getByCatchMessage(message.toUnformattedString()) ?: return@registerGameEvent
            val bobber = mc.player?.fishing
            val checkPos = bobber?.position() ?: mc.player?.position() ?: Vec3.ZERO
            val hotspot = HotSpotEvents.getHotspotAt(checkPos)
            val bait = BaitEventManager.lastBait
            runTasks(sc, isDoubleHook, hotspot, checkPos, bait)
            isDoubleHook = false
        }
    }

    override val runTasks: (SeaCreatures, Boolean, Hotspot?, Vec3, Bait?) -> Unit = { sc, doubleHook, hotspot, pos, bait ->
        safeExecution {
            tasks.forEach { task ->
                task.callback(sc, doubleHook, hotspot, pos, bait)
            }
        }
    }

    class SeaCreatureCatchEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3, bait : Bait?) -> Unit
    ) : ManagedTask<(SeaCreatures, doubleHook : Boolean, hotspot : Hotspot?, pos : Vec3, bait : Bait?) -> Unit, SeaCreatureCatchEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
