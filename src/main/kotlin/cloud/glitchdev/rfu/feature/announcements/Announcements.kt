package cloud.glitchdev.rfu.feature.announcements

import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.announcements.command.Fetch
import cloud.glitchdev.rfu.feature.announcements.command.Open
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.dsl.toInteractiveText
import cloud.glitchdev.rfu.utils.network.AnnouncementsHttp
import net.minecraft.network.chat.Component

@RFUFeature
object Announcements : Feature {
    var announcement: Announcement? = null

    override fun onInitialize() {
        AnnouncementsHttp.getLatestAnnouncement { newAnnouncement ->
            announcement = newAnnouncement
        }

        registerJoinEvent { wasConnected ->
            if (!wasConnected) {
                announcement?.let {
                    Chat.sendMessage(
                        it.message.toInteractiveText(
                            "/rfuannouncement open",
                            Component.literal("Open announcement")
                        )
                    )
                }
            }
        }
    }

    @Command
    object AnnouncementCommand : AbstractCommand("rfuannouncement") {
        override val description: String = "Base command for announcements"

        init {
            append(Fetch)
            append(Open)
        }
    }
}