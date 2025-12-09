package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.gui.PartyFinder
import cloud.glitchdev.rfu.utils.Gui
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient

class RicciosFinestUtilities : ClientModInitializer {

    override fun onInitializeClient() {
        Gui.initialize()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommandManager.literal("rfupf").executes { context ->
                Gui.openGui(PartyFinder())
                1
            })
        }
    }

    companion object {
        val minecraft: MinecraftClient = MinecraftClient.getInstance()
    }
}