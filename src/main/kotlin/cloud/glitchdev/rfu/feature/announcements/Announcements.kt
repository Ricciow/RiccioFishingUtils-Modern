package cloud.glitchdev.rfu.feature.announcements

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.gui.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toInteractiveText
import cloud.glitchdev.rfu.utils.network.AnnouncementsHttp
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.text.Text

@RFUFeature
object Announcements : Feature {
    var hasJoined : Boolean = false
    var announcement : Announcement? = null

    override fun onInitialize() {
        AnnouncementsHttp.getLatestAnnouncement { newAnnouncement ->
            announcement = newAnnouncement
        }

        Command.registerCommand(
            literal("rfulatestannouncement")
                .executes { context ->
                    if(announcement != null) {
                        Gui.openGui(AnnouncementWindow(announcement!!))
                    }
                    else {
                        context.source.sendFeedback(TextUtils.rfuLiteral("Unable to get latest announcement.",
                            TextStyle(TextColor.LIGHT_RED)))
                    }
                    return@executes 1
                }
        )

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if(!hasJoined) {
                hasJoined = true
                announcement?.let { Chat.sendMessage(it.message.toInteractiveText("/rfulatestannouncement", Text.literal("Open announcement"))) }
            }
        }

    }
}