package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.gui.PartyFinder
import cloud.glitchdev.rfu.utils.Gui
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient

class RiccioFishingUtils : ClientModInitializer {
    override fun onInitializeClient() {
        Gui.registerEvents()
        Party.registerEvents()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommandManager.literal("rfupf").executes { context ->
                if(World.isInSkyblock()) {
                    Gui.openGui(PartyFinder())
                } else {
                    context.source.sendFeedback(TextUtils.rfuLiteral("Must be in skyblock to use this feature!",
                        TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)))
                }
                1
            })

            dispatcher.register(ClientCommandManager.literal("rfupartymembers").executes { context ->
                context.source.sendFeedback(TextUtils.rfuLiteral(Party.members.joinToString(),
                    TextStyle(TextColor.WHITE, TextEffects.UNDERLINE)))
                1
            })
        }
    }

    companion object {
        val minecraft: MinecraftClient = MinecraftClient.getInstance()
    }
}