package cloud.glitchdev.rfu.feature.announcements

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.time.Instant

@Command
object ChangelogCommand : SimpleCommand("rfuchangelog") {
    override val description: String = "Opens the RFU changelog"

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("changelog.md")

        if (stream == null) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Changelog not found in mod resources.",
                    TextColor.LIGHT_RED
                )
            )
            return 1
        }

        val content = try {
            stream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Failed to load changelog: ${e.message}"
        }

        Gui.openGui(
            AnnouncementWindow(
                Announcement(
                    id = "changelog",
                    title = "Changelog",
                    message = "Opening Changelog",
                    content = content,
                    issuedAt = Instant.now()
                )
            )
        )
        return 1
    }
}
