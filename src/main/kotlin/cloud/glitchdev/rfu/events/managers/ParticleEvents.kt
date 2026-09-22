package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.wrappers.VoidCancelable
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket

object ParticleEvents : AbstractEventManager<(packet: ClientboundLevelParticlesPacket) -> Unit, ParticleEvents.ParticleEvent>() {
    override val runTasks: (ClientboundLevelParticlesPacket) -> Unit = { packet ->
        safeExecution {
            tasks.forEach { task -> task.callback(packet) }
        }
    }

    fun processPacket(packet: ClientboundLevelParticlesPacket) {
        runTasks(packet)
    }

    fun registerParticleEvent(priority: Int = 20, callback: (packet: ClientboundLevelParticlesPacket) -> Unit): ParticleEvent {
        return ParticleEvent(priority, callback).register()
    }

    class ParticleEvent(
        priority: Int = 20,
        callback: (packet: ClientboundLevelParticlesPacket) -> Unit
    ) : ManagedTask<(ClientboundLevelParticlesPacket) -> Unit, ParticleEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }

    object ParticleRenderEvents : AbstractEventManager<(packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit, ParticleRenderEvents.ParticleRenderEvent>() {
        override val runTasks: (ClientboundLevelParticlesPacket, VoidCancelable) -> Unit = { packet, cancelable ->
            safeExecution {
                tasks.forEach { task -> task.callback(packet, cancelable) }
            }
        }

        fun registerParticleRenderEvent(priority: Int = 20, callback: (packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit): ParticleRenderEvent {
            return ParticleRenderEvent(priority, callback).register()
        }

        class ParticleRenderEvent(
            priority: Int = 20,
            callback: (packet: ClientboundLevelParticlesPacket, cancelable: VoidCancelable) -> Unit
        ) : ManagedTask<(ClientboundLevelParticlesPacket, VoidCancelable) -> Unit, ParticleRenderEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }
}
