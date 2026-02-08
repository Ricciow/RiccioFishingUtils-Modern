package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.config.RFUSettings
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import cloud.glitchdev.rfu.generated.RFULoader
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.impl.FabricLoaderImpl

object RiccioFishingUtils : ClientModInitializer {
    const val MOD_ID = "rfu"
    const val API_URL = "https://rfu.glitchdev.cloud/api"
    val CONFIG_DIR = FabricLoaderImpl.INSTANCE.configDir

    val mc: Minecraft = Minecraft.getInstance()
    val configurator = Configurator(MOD_ID)
    val settings = RFUSettings.register(configurator)

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            RFULoader.loadFeatures()
            RFULoader.registerEvents()
            RFULoader.registerHud()
        }
    }
}