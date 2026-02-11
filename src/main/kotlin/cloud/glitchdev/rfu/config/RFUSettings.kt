package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.utils.Chat
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.Util

object RFUSettings : ConfigKt("rfu/settings") {
    override val name: TranslatableValue
        get() = Literal("RiccioFishingUtils")
    override val description: TranslatableValue
        get() = Literal("Settings for the greatest hit mod RFU")

    init {
        button {
            title = "RFU Discord"
            description = "Join the rfu discord!"
            text = "Join"

            onClick {
                Util.getPlatform().openUri("https://discord.gg/JfrXm6TqXz")
            }
        }

        button {
            title = "Github"
            description = "Contribute to the mod's development! Leave a star <3"
            text = "Open"

            onClick {
                Util.getPlatform().openUri("https://github.com/ricciow/ricciofishingutils-modern")
            }
        }

        button {
            title = "Patreon"
            description = "Help me maintain the servers, not really a must but thanks if you do <3"
            text = "Open"

            onClick {
                Util.getPlatform().openUri("https://www.patreon.com/cw/Ricciow")
            }
        }

        button {
            title = "See Commands"
            description = "See what commands RFU has to offer!"
            text = "See"

            onClick {
                mc.schedule {
                    mc.setScreen(null)

                    Chat.sendCommand("rfuhelp")
                }
            }
        }

        category(GeneralFishing)
        category(LavaFishing)
        category(OtherSettings)
        category(BackendSettings)
        category(DevSettings)
    }
}