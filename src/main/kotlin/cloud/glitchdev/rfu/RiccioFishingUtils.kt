package cloud.glitchdev.rfu

import cloud.glitchdev.rfu.achievement.migration.AchievementMigration
import cloud.glitchdev.rfu.config.RFUSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.migration.ConfigMigration
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import cloud.glitchdev.rfu.generated.RFULoader
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.impl.FabricLoaderImpl

object RiccioFishingUtils : ClientModInitializer {
    const val MOD_ID = "rfu"
    val API_URL = "https://rfu.ricciow.dev/api"
        get() {
            if (DevSettings.devMode && DevSettings.useCustomBackend) return DevSettings.backEndEnvironment
            return field
        }

    val WS_URL: String
        get() {
            if (DevSettings.devMode && DevSettings.useCustomWebSocket) return DevSettings.webSocketEnvironment
            return API_URL.replace("https://", "wss://").replace("http://", "ws://").replace("/api", "") + "/ws"
        }

    val CONFIG_DIR = FabricLoaderImpl.INSTANCE.configDir
    val CONTAINER = FabricLoaderImpl.INSTANCE.getModContainer(MOD_ID).get()
    val RFU_VERSION = CONTAINER.metadata.version

    val mc: Minecraft = Minecraft.getInstance()
    val configurator = Configurator(MOD_ID)

    init {
        ConfigMigration.runMigrations(CONFIG_DIR.resolve("rfu/settings.jsonc"))
        AchievementMigration.runMigrations(CONFIG_DIR.resolve("rfu/data/achievements.json"))
    }

    val settings = RFUSettings.register(configurator)
    private var isInitialized = false

    override fun onInitializeClient() {
        RFULoader.registerInstantEvents()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            if (isInitialized) return@register
            RFULoader.registerChallenges()
            RFULoader.loadFeatures()
            RFULoader.registerCommands()
            RFULoader.registerPartyCommands()
            RFULoader.registerEvents()
            RFULoader.registerHud()
            RFULoader.registerAchievements()
            RFULoader.postInitializeEvents()
            isInitialized = true
        }
    }

    fun saveConfig() {
        configurator.saveConfig(settings)
    }
}