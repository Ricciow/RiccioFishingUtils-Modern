package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable
import net.minecraft.network.protocol.Packet

object PacketSentEvents : AbstractEventManager<(packet: Packet<*>, cancelable: VoidCancelable) -> Unit, PacketSentEvents.PacketSentEvent>() {
    override val runTasks: (Packet<*>, VoidCancelable) -> Unit = { packet, cancelable ->
        safeExecution(mainThread = false) {
            for (task in tasks) {
                task.callback(packet, cancelable)
                if (cancelable.isCancelled()) break
            }
        }
    }

    fun registerPacketSentEvent(
        priority: Int = 20,
        callback: (packet: Packet<*>, cancelable: VoidCancelable) -> Unit
    ): PacketSentEvent {
        return PacketSentEvent(priority, callback).register()
    }

    class PacketSentEvent(
        priority: Int = 20,
        callback: (packet: Packet<*>, cancelable: VoidCancelable) -> Unit
    ) : ManagedTask<(Packet<*>, VoidCancelable) -> Unit, PacketSentEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
