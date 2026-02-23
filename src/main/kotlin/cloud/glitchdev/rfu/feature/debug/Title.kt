package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Title : AbstractCommand("title") {
    override val description: String = "Sends a title message on the Hud."

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

                        debugTitle(argument)

                        1
                    }
            )
    }

    fun debugTitle(argument: String) {
        val text = argument.replace("&".toRegex(), "§")
        Title.showTitle(text, text)
    }
}