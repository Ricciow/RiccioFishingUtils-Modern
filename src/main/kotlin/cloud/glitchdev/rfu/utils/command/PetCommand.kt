package cloud.glitchdev.rfu.utils.command

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.window.BestPetsWindow
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object PetCommand : SimpleCommand("rfupets") {
    override val description: String = "Opens the Best Pets to Level window."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        mc.execute {
            mc.setScreen(BestPetsWindow)
        }
        return 1
    }
}
