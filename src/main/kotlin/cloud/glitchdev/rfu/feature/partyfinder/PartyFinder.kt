package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object PartyFinder : SimpleCommand("rfupf") {
    override val description: String = "Opens the RFU Party Finder window."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!BackendSettings.backendAccepted) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must accept the backend features to use this feature!",
                    TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
                ).append(
                    Component.literal("\n\n${TextColor.LIGHT_RED}/rfu -> Backend Settings -> Connect to Backend")
                )
            )
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

        Gui.openGui(PartyFinderWindow)

        return 1
    }
}