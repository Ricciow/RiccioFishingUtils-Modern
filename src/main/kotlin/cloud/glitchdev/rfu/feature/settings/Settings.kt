package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object Settings : SimpleCommand("rfu") {
    override val description: String = "Opens the RFU settings."

    private val hud_commands = listOf("gui", "hud", "move")

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        super.build(builder)
        hud_commands.forEach { alias ->
            builder.then(lit(alias).executes {
                HudWindow.openEditingGui(HudWindow.EditTarget.HUD)
                1
            })
        }
    }

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        mc.schedule {
            //~ if >=26.2 'setScreen' -> 'gui.setScreen' {
            mc.gui.setScreen(ResourcefulConfigScreen.getFactory(MOD_ID).apply(null))
            //~}
        }

        return 1
    }
}