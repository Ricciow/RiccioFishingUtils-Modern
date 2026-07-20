package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

@Command
object EmojisCommand : SimpleCommand("rfuemojis") {
    override val description: String = "Displays all emojis and their smallest alias"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val text = TextUtils.rfuLiteral("Available Emojis:", TextColor.GOLD)
        Emoji.EMOJIS.forEach { (unicode, aliases) ->
            val smallestAlias = aliases.minByOrNull { it.length } ?: ""
            val formattedEmoji = with(Emoji) { unicode.whiteText() }
            text.append(Component.literal("\n${TextColor.GRAY}- $formattedEmoji "))
                .append(Component.literal("${TextColor.YELLOW}:"))
                .append(Component.literal("${TextColor.GOLD}$smallestAlias"))
                .append(Component.literal("${TextColor.YELLOW}:"))
        }
        context.source.sendFeedback(text)
        return 1
    }
}
