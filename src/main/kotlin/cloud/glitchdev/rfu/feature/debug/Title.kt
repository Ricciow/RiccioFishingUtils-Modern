package cloud.glitchdev.rfu.feature.debug

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