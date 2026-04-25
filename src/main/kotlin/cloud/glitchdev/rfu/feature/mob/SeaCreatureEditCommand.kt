package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.window.SeaCreatureEditWindow
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object SeaCreatureEditCommand : AbstractCommand("rfuscedit") {
    override val description: String = "Opens the Sea Creature Edit window"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes {
            mc.schedule {
                mc.setScreen(SeaCreatureEditWindow(null))
            }
            1
        }
    }
}
