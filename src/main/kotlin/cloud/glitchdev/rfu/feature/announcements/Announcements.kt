package cloud.glitchdev.rfu.feature.announcements

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.gui.window.AnnouncementWindow
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toInteractiveText
import cloud.glitchdev.rfu.utils.network.AnnouncementsHttp
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.minecraft.network.chat.Component
import java.time.Instant

@RFUFeature
object Announcements : Feature {
    var announcement: Announcement? = null

    override fun onInitialize() {
        AnnouncementsHttp.getLatestAnnouncement { newAnnouncement ->
            announcement = newAnnouncement
        }

        Command.registerCommand(
            literal("rfulatestannouncement")
                .executes { context ->
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

                    return@executes 1
                }
        )

        Command.registerCommand(
            literal("rfuopenannouncement")
                .executes { context ->
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

                    return@executes 1
                }
        )

        registerJoinEvent { wasConnected ->
            if (!wasConnected) {
                announcement?.let {
                    Chat.sendMessage(
                        it.message.toInteractiveText(
                            "/rfuopenannouncement",
                            Component.literal("Open announcement")
                        )
                    )
                }
            }
        }

    }
}