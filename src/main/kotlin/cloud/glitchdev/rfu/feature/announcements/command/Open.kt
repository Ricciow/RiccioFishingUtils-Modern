package cloud.glitchdev.rfu.feature.announcements.command

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.announcements.Announcements.announcement
import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.time.Instant

object Open : SimpleCommand("open") {
    override val description: String = "Opens the announcement window with the currently loaded announcement."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (announcement != null) {
            Gui.openGui(
                AnnouncementWindow(
                    announcement ?: Announcement(
                        "Nancy Announcement",
                        "This is an error",
                        "Hmm i shouldnt be here",
                        "Hmm i shouldnt be here",
                        Instant.now()
                    )
                )
            )
        } else {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Unable to get latest announcement.",
                    TextStyle(TextColor.LIGHT_RED)
                )
            )
        }

        return 1
    }
}