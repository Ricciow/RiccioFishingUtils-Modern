package cloud.glitchdev.rfu.feature.announcements

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.AnnouncementEvents.registerAnnouncementUpdateEvent
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.announcements.command.Open
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.dsl.toInteractiveText
import cloud.glitchdev.rfu.utils.network.Network
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.util.InstantTypeAdapter
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import java.time.Instant

@RFUFeature
object Announcements : Feature {
    var announcement: Announcement? = null
    private var lastShownId: String? = null

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()

    override fun onInitialize() {
        registerAnnouncementUpdateEvent { newAnnouncement ->
            val isNew = newAnnouncement != null && (announcement == null || announcement?.id != newAnnouncement.id)
            
            if (isNew) {
                sendAnnouncementMessage(newAnnouncement)
            }
            announcement = newAnnouncement
        }

        registerJoinEvent(delayMillis = 3000) { wasConnected ->
            if (!wasConnected) {
                if (!WebSocketClient.isConnected) {
                    fetchLatestAnnouncement { fetched ->
                        fetched?.let { sendAnnouncementMessage(it) }
                    }
                } else {
                    announcement?.let { sendAnnouncementMessage(it) }
                }
            }
        }
    }

    private fun sendAnnouncementMessage(announcement: Announcement) {
        if (announcement.id == lastShownId) return
        lastShownId = announcement.id

        Chat.sendMessage(
            announcement.message.toInteractiveText(
                "/rfuannouncement open",
                Component.literal("Open announcement")
            )
        )
    }

    fun fetchLatestAnnouncement(callback: (Announcement?) -> Unit = {}) {
        Network.getRequest("${API_URL}/announcement/latest") { response ->
            if (response.isSuccessful() && response.body != null) {
                try {
                    val latest = gson.fromJson(response.body, Announcement::class.java)
                    announcement = latest
                    callback(latest)
                } catch (e: Exception) {
                    RFULogger.error("Error while fetching latest announcement: ", e)
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }

    @Command
    object AnnouncementCommand : AbstractCommand("rfuannouncement") {
        override val description: String = ""

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder.executes { context ->
                Open.execute(context)
            }
        }

        init {
            append(Open)
        }
    }
}
