package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.gui.PartyFinder
import cloud.glitchdev.rfu.utils.Gui
import gg.essential.universal.UScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

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