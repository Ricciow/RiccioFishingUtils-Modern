package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.config.RFUSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import cloud.glitchdev.rfu.generated.RFULoader
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.impl.FabricLoaderImpl

object RiccioFishingUtils : ClientModInitializer {
    const val MOD_ID = "rfu"
    val API_URL = "https://rfu.glitchdev.cloud/api"
        get() {
            if(DevSettings.devMode) return DevSettings.backEndEnvironment
            return field
        }

    val CONFIG_DIR = FabricLoaderImpl.INSTANCE.configDir

    val mc: Minecraft = Minecraft.getInstance()
    val configurator = Configurator(MOD_ID)
    val settings = RFUSettings.register(configurator)

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            RFULoader.loadFeatures()
            RFULoader.registerCommands()
            RFULoader.registerEvents()
            RFULoader.registerHud()
        }
    }

    fun saveConfig() {
        configurator.saveConfig(settings)
    }
}