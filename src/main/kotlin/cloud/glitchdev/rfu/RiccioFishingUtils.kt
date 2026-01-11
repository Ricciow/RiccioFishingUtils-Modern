package cloud.glitchdev.rfu

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import cloud.glitchdev.rfu.generated.RFULoader

class RiccioFishingUtils : ClientModInitializer {
    override fun onInitializeClient() {
        RFULoader.loadFeatures()
        RFULoader.registerEvents()
    }

    companion object {
        val minecraft: MinecraftClient = MinecraftClient.getInstance()
        val API_URL: String = "https://rfu.glitchdev.cloud/api"
    }
}