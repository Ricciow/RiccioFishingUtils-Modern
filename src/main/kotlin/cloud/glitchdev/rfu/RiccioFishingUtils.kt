package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.utils.Command
import cloud.glitchdev.rfu.utils.Features
import cloud.glitchdev.rfu.utils.Gui
import cloud.glitchdev.rfu.utils.network.Network
import cloud.glitchdev.rfu.utils.Party
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient

class RiccioFishingUtils : ClientModInitializer {
    override fun onInitializeClient() {
        Features.initializeFeatures()

        Gui.registerEvents()
        Party.registerEvents()
        Network.registerEvents()


        Command.registerEvents()
    }

    companion object {
        val minecraft: MinecraftClient = MinecraftClient.getInstance()
        val API_URL: String = "https://rfu.glitchdev.cloud/api"
    }
}