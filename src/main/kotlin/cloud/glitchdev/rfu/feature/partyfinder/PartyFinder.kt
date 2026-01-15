package cloud.glitchdev.rfu.feature.partyfinder

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.config.dev.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.gui.PartyFinderWindow
import cloud.glitchdev.rfu.utils.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal

@RFUFeature
object PartyFinder : Feature {
    override fun onInitialize() {
        Command.registerCommand(
            literal("rfupf")
                .executes { context ->
                    if(World.isInSkyblock() || DevSettings.devMode) {
                        Gui.openGui(PartyFinderWindow())
                    } else {
                        context.source.sendFeedback(TextUtils.rfuLiteral("Must be in skyblock to use this feature!",
                            TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)))
                    }
                    return@executes 1
                }
        )

        //Preload party finder window so it doesn't lag upon first opening
        minecraft.execute {
            PartyFinderWindow()
        }
    }
}