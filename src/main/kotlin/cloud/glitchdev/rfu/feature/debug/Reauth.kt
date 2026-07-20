package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.network.Network
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Reauth : AbstractCommand("reauth") {
    override val description: String = "Force re-authenticates the RFU backend connection."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Re-authenticating backend...",
                    TextStyle(TextColor.YELLOW)
                )
            )
            Network.reauthenticate()
            1
        }
    }
}
