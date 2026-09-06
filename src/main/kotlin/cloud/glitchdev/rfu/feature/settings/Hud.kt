package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object Hud : AbstractCommand("rfumove") {
    override val description: String = "Opens the GUI to move Hud Elements or reset to defaults."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            HudWindow.openEditingGui()
            1
        }
        builder.then(
            lit("reset").executes {
                HudWindow.resetAllToDefaults()
                1
            }
        )
    }
}