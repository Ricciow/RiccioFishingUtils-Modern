package cloud.glitchdev.rfu.feature.settings

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.RiccioFishingUtils.settings
import cloud.glitchdev.rfu.events.managers.CloseConfigEvents.registerCloseConfigEvent
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
    private var onClose: () -> Unit = {}

    init {
        registerCloseConfigEvent { onClose() }
    }

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
        open()
        return 1
    }

    fun open(query: String = "", onClose: () -> Unit = {}) {
        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        mc.schedule {
            if (HudWindow.isEditingOpen) HudWindow.onWindowClose()
            val screen = ResourcefulConfigScreen.make(settings).withQuery(query).build()
            this.onClose = {
                if (mc.gui.screen() === screen) {
                    this.onClose = {}
                    mc.schedule { onClose() }
                }
            }
            mc.gui.setScreen(screen)
        }
        //~}
    }
}
