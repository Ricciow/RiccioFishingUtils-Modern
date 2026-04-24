package cloud.glitchdev.rfu.feature.other.ignore

import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object HotspotIgnoreCommand : SimpleCommand("rfuignore") {
    override val description: String = "Manage ignored users for hotspots and parties"

    init {
        append(AddIgnoreSubCommand)
        append(RemoveIgnoreSubCommand)
        append(RemoveAllIgnoreSubCommand)
        append(ListIgnoreSubCommand)
    }

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        IgnoreUtils.showHelp(context.source)
        return 1
    }
}
