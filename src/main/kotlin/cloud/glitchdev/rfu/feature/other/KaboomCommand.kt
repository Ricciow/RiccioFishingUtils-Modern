package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object KaboomCommand : SimpleCommand("kaboom") {
    override val description: String = "Sends :kaboom: in party chat and leaves the party."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!Party.inParty) {
            context.source.sendFeedback(TextUtils.rfuLiteral("You are not in a party!", TextColor.LIGHT_RED))
            return 1
        }

        Chat.sendPartyMessage(":kaboom:")
        Chat.sendCommand("p leave")
        return 1
    }
}
