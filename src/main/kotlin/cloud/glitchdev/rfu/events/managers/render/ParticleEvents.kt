package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket

@EventManager(register = false)
object ParticleEvents : AbstractEventManager<(packet: ClientboundLevelParticlesPacket) -> Unit, ParticleEvents.ParticleEvent>() {
    override val runTasks: (ClientboundLevelParticlesPacket) -> Unit = { packet ->
        safeExecution {
            tasks.forEach { task -> task.callback(packet) }
        }
    }

    fun processPacket(packet: ClientboundLevelParticlesPacket) {
        runTasks(packet)
    }

    fun registerParticleEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (packet: ClientboundLevelParticlesPacket) -> Unit): ParticleEvent {
        return ParticleEvent(priority, callback).register()
    }

    class ParticleEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (packet: ClientboundLevelParticlesPacket) -> Unit
    ) : ManagedTask<(ClientboundLevelParticlesPacket) -> Unit, ParticleEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
