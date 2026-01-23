package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

object WorldChangeEvents : AbstractEventManager<(ClientPlayNetworkHandler, PacketSender, MinecraftClient) -> Unit, WorldChangeEvents.WorldChangeEvent>(), RegisteredEvent {
    override fun register() {
        ClientPlayConnectionEvents.JOIN.register{ handler, sender, client->
            tasks.forEach { task ->
                task.callback(handler, sender, client)
            }
        }
    }

    fun registerWorldChangeEvent(priority: Int = 20, callback: (ClientPlayNetworkHandler, PacketSender, MinecraftClient) -> Unit): WorldChangeEvent {
        return WorldChangeEvent(priority, callback).register()
    }

    class WorldChangeEvent(
        priority: Int = 20,
        callback: (ClientPlayNetworkHandler, PacketSender, MinecraftClient) -> Unit
    ) : ManagedTask<(ClientPlayNetworkHandler, PacketSender, MinecraftClient) -> Unit, WorldChangeEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}