package cloud.glitchdev.rfu.utils.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

abstract class SimpleCommand(name : String) : AbstractCommand(name) {
    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            execute(context)
        }
    }

    abstract fun execute(context : CommandContext<FabricClientCommandSource>) : Int
}