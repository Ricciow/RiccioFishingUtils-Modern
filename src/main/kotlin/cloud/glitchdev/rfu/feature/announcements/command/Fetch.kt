package cloud.glitchdev.rfu.feature.announcements.command

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.announcements.Announcements.announcement
import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.network.AnnouncementsHttp
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Fetch : SimpleCommand("fetch") {
    override val description: String = "Fetches the latest announcement and opens the announcement window."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        AnnouncementsHttp.getLatestAnnouncement { newAnnouncement ->
            if (newAnnouncement != null) {
                Gui.openGui(AnnouncementWindow(newAnnouncement))
            } else {
                context.source.sendFeedback(
                    TextUtils.rfuLiteral(
                        "Unable to get latest announcement.",
                        TextStyle(TextColor.LIGHT_RED)
                    )
                )
            }

            announcement = newAnnouncement
        }

        return 1
    }
}