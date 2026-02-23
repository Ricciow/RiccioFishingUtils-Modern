package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ChatEvents
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object Chat : AbstractCommand("chat") {
    override val description: String = "Sends a test message in chat which will be processed by ChatEvents."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .then(
                arg("message", StringArgumentType.greedyString())
                    .executes { context ->
                        if(!DevSettings.devMode) {
                            context.source.sendFeedback(
                                TextUtils.rfuLiteral(
                                    "Must have developer mode on to use this feature!",
                                    TextStyle(TextColor.RED, TextEffects.BOLD)
                                )
                            )
                            return@executes 1
                        }

                        val argument = StringArgumentType.getString(context, "message")

                        debugChat(argument)

                        1
                    }
            )
    }

    fun debugChat(argument: String) {
        val text = Component.literal(argument.removeSurrounding("\""))
        Chat.sendMessage(text)
        ChatEvents.ChatEventManager.runTasks(text)
        ChatEvents.GameEventManager.runTasks(text, true)
    }
}