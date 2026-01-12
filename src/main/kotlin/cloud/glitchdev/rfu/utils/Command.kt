package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@AutoRegister
object Command : RegisteredEvent {
    val commands = mutableListOf<LiteralArgumentBuilder<FabricClientCommandSource>>()

    override fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commands.forEach { command ->
                dispatcher.register(command)
            }
        }
    }

    fun registerCommand(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        commands.add(command)
    }

    fun registerCommand(command : String, callback : (context : CommandContext<FabricClientCommandSource>) -> Int) {
        commands.add(literal(command).executes(callback))
    }
}