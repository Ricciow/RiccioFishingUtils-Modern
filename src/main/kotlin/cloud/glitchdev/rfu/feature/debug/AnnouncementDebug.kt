package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement as ModelAnnouncement
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.time.Instant

object AnnouncementDebug : AbstractCommand("announcement") {
    override val description: String = "Opens the announcement window with mock data."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            val mockAnnouncement = ModelAnnouncement(
                id = "debug_announcement",
                title = "# v1.17.0 - Daily Fishing",
                message = "",
                content = """
                    ### Features
                    - Added a daily streak and challenges system with scaling targets, HUD overlay, and /rfudailies GUI
                    - Extended the survivalist achievement with 4 new stages
                    - Added 5 new achievements
                    - Added a setting to adjust hotspot highlight border opacity
                    - Added a new Bloodshot requirement in Party Finder
                    - Added :pod: :silk: :hog: :exploding_head: :boom: emojis
                    
                    ### Fixes
                    - Fixed other messages being counted as trophy catches when they shouldn't
                    - Fixed togglewarp not auto re-joining the party
                    - Fixed an issue where pressing enter really fast would not complete the emoji properly
                    - Fixed hotspot sea creature counts not couting properly on torrhus
                    - Fixed the dye achievements not triggering on vincent menu
                    
                    ### Changes
                    - Reduced Squid Collection achievement to max out at 2M Collection
                    - Reduced Ink Obsessed achievement to cap out at 100k
                    - Added a 5s cooldown between creating party finder entries
                    - Made the party finder alerts off by default
                      - Note: This was mostly meant for when there weren't many users of rfupf, since it is now somewhat relevant, this doesn't have much purpose anymore
                    - Made outdated cake alert check the tablist on a fast loop to clear false expired cake entries when all cakes are active
                    - Removed rfuresetcakes command alias
                    - Removed the bottom border of hotspot highlight
                    
                    ### Back-end
                    - Added current equipment tracking
                    - Added automatic backups for config and data.
                    - Made requisites validation for Enderman 9, Looting 5 and Fishing Level come from back-end.
                """.trimIndent(),
                issuedAt = Instant.now()
            )

            Gui.openGui(AnnouncementWindow(mockAnnouncement))
            1
        }
    }
}
