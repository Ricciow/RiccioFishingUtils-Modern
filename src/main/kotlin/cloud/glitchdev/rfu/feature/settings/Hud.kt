package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object Hud : SimpleCommand("rfumove") {
    override val description: String = "Opens the GUI to move Hud Elements."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        HudWindow.openEditingGui()
        return 1
    }
}