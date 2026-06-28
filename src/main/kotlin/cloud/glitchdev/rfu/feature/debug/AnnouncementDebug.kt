package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement as ModelAnnouncement
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.time.Instant

object AnnouncementDebug : AbstractCommand("announcement") {
    override val description: String = "Opens the announcement window with mock data."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            if (!DevSettings.devMode) {
                context.source.sendFeedback(
                    TextUtils.rfuLiteral(
                        "Must have developer mode on to use this feature!",
                        TextStyle(TextColor.RED, TextEffects.BOLD)
                    )
                )
                return@executes 1
            }

            val mockAnnouncement = ModelAnnouncement(
                id = "debug_announcement",
                title = "RiccioFishingUtils v2.0.0 Update!",
                message = "RFU v2.0.0 is now available! Check out the redesigned UI.",
                content = """
                    # RiccioFishingUtils v2.0.0 Release
                    
                    Welcome to the **next generation** of *fishing utilities* for Hypixel SkyBlock! This update brings a complete redesign of the user interfaces to be more modern and consistent.
                    
                    ## 🚀 Major Additions
                    
                    1. **Redesigned Announcement UI**
                       - Styled to match Party Finder, Pets, and Achievements.
                       - Clean layout with a dedicated scrollable viewport.
                    2. **Performance Enhancements**
                       - Optimized rendering pipelines using `Elementa`.
                       - Fixed multiple memory leaks in connection and chat event handlers.
                    
                    ---
                    
                    ## 📦 Features Showcase
                    
                    > "This is by far the best fishing mod update in 2026!"
                    > — *An avid SkyBlock Fisherman*
                    
                    ### 💻 Code Highlight
                    Here is a snippet showing how easy it is to register new features:
                    
                    ```kotlin
                    @RFUFeature
                    object MyFeature : Feature {
                        override fun onInitialize() {
                            registerTickEvent(interval = 20) {
                                // Your cool logic here!
                            }
                        }
                    }
                    ```
                    
                    ### 🔗 Useful Links
                    * Join our [Discord Server](https://discord.gg/rfu)
                    * Check out the [GitHub Repository](https://github.com/Ricciofishing/RicciosFinestUtilities)

                    *Thank you for supporting RiccioFishingUtils! Good luck with your catches!*
                """.trimIndent(),
                issuedAt = Instant.now()
            )

            Gui.openGui(AnnouncementWindow(mockAnnouncement))
            1
        }
    }
}
