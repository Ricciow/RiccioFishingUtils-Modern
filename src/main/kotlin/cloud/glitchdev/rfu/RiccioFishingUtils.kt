package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.config.RFUSettings
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import cloud.glitchdev.rfu.generated.RFULoader
import cloud.glitchdev.rfu.utils.Command
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator

class RiccioFishingUtils : ClientModInitializer {
    val configurator : Configurator = Configurator(MOD_ID)

    override fun onInitializeClient() {
        configurator.register(RFUSettings::class.java)
        RFULoader.loadFeatures()
        RFULoader.registerEvents()

        Command.registerCommand("rfu") { _ ->
            minecraft.send {
                minecraft.setScreen(ResourcefulConfigScreen.getFactory(MOD_ID).apply(null))
            }
            return@registerCommand 1
        }
    }

    companion object {
        val MOD_ID = "rfu"
        val minecraft: MinecraftClient = MinecraftClient.getInstance()
        const val API_URL: String = "https://rfu.glitchdev.cloud/api"
    }
}