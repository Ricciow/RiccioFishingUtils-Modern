package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.manager.mob.MobManager
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

object Entities : SimpleCommand("entities") {
    override val description: String = "Sends a message with the rfu entities"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val text = MobManager.getEntities().map { entity ->
            Component.literal("\n$entity")
                .withStyle(
                    Style.EMPTY
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("${entity.nameTagEntity}\n\n${entity.modelEntity}")))
                )
        }.fold(TextUtils.rfuLiteral("Debug")) { text, entity ->
            text.append(entity)
        }

        context.source.sendFeedback(text)

        return 1
    }
}