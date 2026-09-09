package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object RequeueParty : SimpleCommand("rfurequeue") {
    override val description: String = "Requeues the previous party listing in Party Finder."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        PartyWebSocket.requeueParty()
        return 1
    }
}
