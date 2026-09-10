package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

@Command
object EmojisCommand : SimpleCommand("rfuemojis") {
    override val description: String = "Displays all available emojis"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val totalCount = Emoji.EMOJIS.size
        val message = TextUtils.rfuLiteral("Available Emojis $GRAY($GOLD$totalCount$GRAY):", GOLD)

        Emoji.EMOJIS.entries.forEachIndexed { index, (unicode, aliases) ->
            if (index % 14 == 0) {
                message.append(Component.literal("\n "))
            }
            message.append(buildEmojiComponent(unicode, aliases))
        }

        message.append(
            Component.literal("\n$DARK_GRAY------------------------------------------\n$GRAY(Hover over an emoji for aliases. Click to insert!)")
        )

        context.source.sendFeedback(message)
        return 1
    }

    private fun buildEmojiComponent(unicode: String, aliases: List<String>): MutableComponent {
        val primaryAlias = aliases.firstOrNull() ?: ""
        val longestAlias = aliases.maxByOrNull { it.length } ?: primaryAlias
        val displayName = Emoji.formatDisplayName(longestAlias)

        val hoverText = Component.literal("$GOLD§l$displayName $WHITE$unicode\n")
        hoverText.append(Component.literal("$GRAY" + "Aliases:\n"))
        aliases.forEach { alias ->
            hoverText.append(Component.literal(" $YELLOW• $GOLD:$GOLD$alias:\n"))
        }
        hoverText.append(Component.literal("\n$YELLOW" + "Click to insert $GOLD:$GOLD$primaryAlias: $YELLOW" + "into chat!"))

        val style = Style.EMPTY
            .withColor(ChatFormatting.WHITE)
            .withHoverEvent(HoverEvent.ShowText(hoverText))
            .withClickEvent(ClickEvent.SuggestCommand(":$primaryAlias:"))

        return Component.literal("$unicode  ").setStyle(style)
    }
}
