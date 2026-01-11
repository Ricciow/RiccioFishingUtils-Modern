package cloud.glitchdev.rfu.utils

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Command {
    val commands = mutableListOf<LiteralArgumentBuilder<FabricClientCommandSource>>()

    fun registerEvents() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commands.forEach { command ->
                dispatcher.register(command)
            }
        }
    }

    fun registerCommand(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        commands.add(command)
    }
}