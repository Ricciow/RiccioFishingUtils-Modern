package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket

@EventManager(register = false)
object ParticleRenderEvents : AbstractEventManager<(packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit, ParticleRenderEvents.ParticleRenderEvent>() {
    override val runTasks: (ClientboundLevelParticlesPacket, VoidCancelable) -> Unit = { packet, cancelable ->
        safeExecution {
            tasks.forEach { task -> task.callback(packet, cancelable) }
        }
    }

    fun registerParticleRenderEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit): ParticleRenderEvent {
        return ParticleRenderEvent(priority, callback).register()
    }

    class ParticleRenderEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit
    ) : ManagedTask<(ClientboundLevelParticlesPacket, VoidCancelable) -> Unit, ParticleRenderEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
