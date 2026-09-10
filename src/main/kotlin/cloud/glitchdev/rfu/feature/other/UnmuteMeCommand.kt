package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object UnmuteMeCommand : SimpleCommand("rfuunmuteme") {
    override val description: String = "Clears the saved mute timer."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!Chat.isMuted) {
            context.source.sendFeedback(TextUtils.rfuLiteral("You are not marked as muted.", TextColor.LIGHT_RED))
            return 1
        }

        Chat.muteExpiration = null
        context.source.sendFeedback(TextUtils.rfuLiteral("Successfully cleared mute timer.", TextColor.LIGHT_GREEN))
        return 1
    }
}
