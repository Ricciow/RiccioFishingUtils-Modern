package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.config.RFUSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.migration.ConfigMigration
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import cloud.glitchdev.rfu.generated.RFULoader
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.impl.FabricLoaderImpl
//? if = 1.21.10 {
/*import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
*///?}

object RiccioFishingUtils : ClientModInitializer {
    const val MOD_ID = "rfu"
    val API_URL = "https://rfu.glitchdev.cloud/api"
        get() {
            if(DevSettings.devMode) return DevSettings.backEndEnvironment
            return field
        }

    val CONFIG_DIR = FabricLoaderImpl.INSTANCE.configDir
    val CONTAINER = FabricLoaderImpl.INSTANCE.getModContainer(MOD_ID).get()
    val RFU_VERSION = CONTAINER.metadata.version

    val mc: Minecraft = Minecraft.getInstance()
    val configurator = Configurator(MOD_ID)

    init {
        ConfigMigration.runMigrations(CONFIG_DIR.resolve("rfu/settings.jsonc"))
    }

    val settings = RFUSettings.register(configurator)

    override fun onInitializeClient() {
        RFULoader.registerInstantEvents()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            RFULoader.loadFeatures()
            RFULoader.registerCommands()
            RFULoader.registerEvents()
            RFULoader.registerHud()
            RFULoader.registerAchievements()
            //? if = 1.21.10 {
            /*registerJoinEvent { wasConnected ->
                if(!wasConnected) {
                    Chat.sendMessage(TextUtils.rfuLiteral("This version is no longer supported! Update to 1.21.11 or newer for the latest content!",
                        TextColor.LIGHT_RED))
                }
            }
            *///?}
        }

    }

    fun saveConfig() {
        configurator.saveConfig(settings)
    }
}