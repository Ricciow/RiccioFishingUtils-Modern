package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.window.EmojiEditWindow
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object EmojisCommand : SimpleCommand("rfuemojis") {
    override val description = "Opens the emoji editor"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        mc.schedule {
            //~ if >=26.2 'setScreen' -> 'gui.setScreen' {
            mc.gui.setScreen(EmojiEditWindow())
            //~}
        }
        return 1
    }
}
