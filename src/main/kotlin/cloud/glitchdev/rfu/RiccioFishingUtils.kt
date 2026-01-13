package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.config.RFUSettings
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import cloud.glitchdev.rfu.generated.RFULoader
import com.teamresourceful.resourcefulconfig.api.loader.Configurator

object RiccioFishingUtils : ClientModInitializer {
    const val MOD_ID = "rfu"
    const val API_URL: String = "https://rfu.glitchdev.cloud/api"

    val minecraft: MinecraftClient = MinecraftClient.getInstance()
    val configurator = Configurator(MOD_ID)
    val settings = RFUSettings.register(configurator)

    override fun onInitializeClient() {
        RFULoader.loadFeatures()
        RFULoader.registerEvents()
    }
}