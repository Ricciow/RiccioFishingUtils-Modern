package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.seacreatures.SeaCreatureSettingsManager
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ReloadSeaCreatures : AbstractCommand("reloadsc") {
    override val description: String = "Reloads sea creatures from resources"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            val count = SeaCreatureSettingsManager.reloadFromResources()
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Reloaded $count sea creatures from resources.",
                    TextStyle(TextColor.LIGHT_GREEN)
                )
            )
            1
        }
    }
}