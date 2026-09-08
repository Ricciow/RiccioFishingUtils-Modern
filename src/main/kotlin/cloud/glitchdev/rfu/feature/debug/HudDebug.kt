package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object HudDebug : AbstractCommand("hud") {
    override val description: String = "HUD debug and design commands."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            lit("export")
                .executes {
                    mc.schedule {
                        HudWindow.openExportGui(HudWindow.EditTarget.HUD)
                    }
                    1
                }
                .then(
                    lit("hud").executes {
                        mc.schedule {
                            HudWindow.openExportGui(HudWindow.EditTarget.HUD)
                        }
                        1
                    }
                )
                .then(
                    lit("inv").executes {
                        mc.schedule {
                            HudWindow.openExportGui(HudWindow.EditTarget.INVENTORY)
                        }
                        1
                    }
                )
                .then(
                    lit("inventory").executes {
                        mc.schedule {
                            HudWindow.openExportGui(HudWindow.EditTarget.INVENTORY)
                        }
                        1
                    }
                )
        )
    }
}
