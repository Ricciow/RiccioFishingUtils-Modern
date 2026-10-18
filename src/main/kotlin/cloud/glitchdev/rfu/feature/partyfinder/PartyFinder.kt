package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

@Command
object PartyFinder : SimpleCommand("rfupf") {
    override val description: String = "Opens the RFU Party Finder window."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!BackendSettings.backendAccepted) {
            context.source.sendFeedback(TextUtils.backendAcceptMessage())
            return 1
        }

        if (!World.isInSkyblock) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must be in skyblock to use this feature!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
                )
            )
            return 1
        }

        if (World.isOnAlpha) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Party Finder is disabled on the Alpha network!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
                )
            )
            return 1
        }

        if (!WebSocketClient.isConnected) {
            val description = buildString {
                append("${TextColor.GRAY}Status: ${WebSocketClient.connectionStatus}\n")
                WebSocketClient.lastConnectionIssue?.let { append("${TextColor.GRAY}Last issue: ${TextColor.LIGHT_RED}$it\n") }
                append("\n")
                append("${TextColor.YELLOW}Click or use /rfubackend reconnect to retry the connection.\n")
                append("If this keeps happening, check latest.log for details.\n\n")
                append("For DNS issues, try Cloudflare DNS (1.1.1.1 / 1.0.0.1), then restart Minecraft.")
            }
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "RFU Websocket isn't connected! (Hover for help)",
                    TextStyle(TextColor.LIGHT_RED)
                ).withStyle { style ->
                    style.withHoverEvent(HoverEvent.ShowText(Component.literal(
                        description
                    ))).withClickEvent(ClickEvent.RunCommand("/rfubackend reconnect"))
                }
            )
            return 1
        }

        Gui.openGui(PartyFinderWindow)

        return 1
    }
}
