package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ChatEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.StringSuggestionProvider
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.minecraft.text.Text

@RFUFeature
object DebugCommand : Feature {
    override fun onInitialize() {
        Command.registerCommand(
            literal("rfudebug")
                .then(
                    argument("feature", StringArgumentType.string())
                        .suggests(StringSuggestionProvider(listOf("chat")))
                        .then(
                            argument("argument", StringArgumentType.greedyString())
                                .executes { context ->
                                    if (!DevSettings.devMode) {
                                        context.source.sendFeedback(
                                            TextUtils.rfuLiteral(
                                                "Must have developer mode on to use this feature!",
                                                TextStyle(TextColor.RED, TextEffects.BOLD)
                                            )
                                        )
                                    }

                                    val feature = StringArgumentType.getString(context, "feature")
                                    val argument = StringArgumentType.getString(context, "argument")

                                    when (feature) {
                                        "chat" -> debugChat(argument)
                                    }

                                    1
                                }
                        )
                )
        )
    }

    fun debugChat(argument: String) {
        val text = Text.literal(argument.removeSurrounding("\""))
        Chat.sendMessage(text)
        ChatEvents.ChatEventManager.runTasks(text)
        ChatEvents.GameEventManager.runTasks(text, true)
    }
}