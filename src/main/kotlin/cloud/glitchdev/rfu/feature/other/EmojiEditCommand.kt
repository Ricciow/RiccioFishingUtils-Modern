package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object EmojiEditCommand : SimpleCommand("rfuemojiedit") {
    override val description = EmojisCommand.description

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int = EmojisCommand.execute(context)
}