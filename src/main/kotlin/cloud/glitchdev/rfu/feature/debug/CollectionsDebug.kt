package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.collections.CollectionItem
import cloud.glitchdev.rfu.data.collections.CollectionsHandler
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import java.util.Locale

object CollectionsDebug : SimpleCommand("collections") {
    override val description: String = "Shows tracked collection totals."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        context.source.sendFeedback(TextUtils.debugLiteral("--- Collections Debug ---", TextColor.GOLD))
        for (item in CollectionItem.entries) {
            val total = String.format(Locale.US, "%,d", CollectionsHandler.get(item))
            context.source.sendFeedback(Component.literal(
                "${TextColor.YELLOW}${item.displayName} ${TextColor.GRAY}(${item.name}): ${TextColor.LIGHT_GREEN}$total"
            ))
        }
        return 1
    }
}
