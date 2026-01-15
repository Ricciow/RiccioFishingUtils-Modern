package cloud.glitchdev.rfu.utils

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Command {
    fun registerCommand(command: LiteralArgumentBuilder<FabricClientCommandSource>) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(command)
        }
    }

    fun registerCommand(command : String, callback : (context : CommandContext<FabricClientCommandSource>) -> Int) {
        registerCommand(literal(command).executes(callback))
    }
}