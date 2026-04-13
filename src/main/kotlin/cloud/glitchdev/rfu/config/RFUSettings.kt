package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.BackendSettings
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.config.categories.DropsSettings
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.config.categories.JerryFishing
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.migration.ConfigMigration
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.network.VersionHttp.isOutdated
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
//~ if <1.21.11 'net.minecraft.util.Util' -> 'net.minecraft.Util' {
import net.minecraft.util.Util
//~}

object RFUSettings : ConfigKt("rfu/settings") {
    override val name: TranslatableValue
        get() = Literal("RiccioFishingUtils")
    override val description: TranslatableValue
        get() = Literal("Settings for the greatest hit mod RFU")

    init {
        separator {
            title = "Your mod is up to date!"
            description = "Good job!"
            condition = { !isOutdated }
        }

        separator {
            title = "Your mod is outdated!"
            description = "You should update!"
            condition = { isOutdated }
        }

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
            title = "Achievements"
            description = "See your rfu achievements!"
            text = "See"

            onClick {
                mc.schedule {
                    mc.setScreen(null)

                    Chat.sendCommand("rfuachievements")
                }
            }
        }

        button {
            title = "Party Finder"
            description = "Click it or use /rfupf!"
            text = "Open"

            onClick {
                mc.schedule {
                    mc.setScreen(null)

                    Chat.sendCommand("rfupf")
                }
            }
        }

        button {
            title = "Move Hud"
            description = "Click it or use /rfumove!"
            text = "Move"

            onClick {
                mc.schedule {
                    mc.setScreen(null)

                    Chat.sendCommand("rfumove")
                }
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
        category(HotSpotSettings)
        category(InkFishing)
        category(JerryFishing)
        category(SeaCreatureConfig)
        category(DropsSettings)
        category(OtherSettings)
        category(BackendSettings)
        category(DevSettings)
    }

    var rfuConfigVersion by int(ConfigMigration.CURRENT_VERSION) {
        name = Literal("Config Version")
        description = Literal("Internal version used for config migrations.")
        condition = { false }
    }
}