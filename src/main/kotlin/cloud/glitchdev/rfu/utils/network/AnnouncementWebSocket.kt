package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.feature.announcements.Announcements
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.dsl.toInteractiveText
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.util.InstantTypeAdapter
import java.time.Instant

@AutoRegister
object AnnouncementWebSocket : RegisteredEvent {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()

    override fun register() {
        RFULogger.dev("Registering AnnouncementWebSocket")
        val callback : (String) -> Unit = { msg ->
            try {
                RFULogger.dev("Received message on AnnouncementWebSocket: ${msg.take(100)}...")
                val type = object : TypeToken<WebSocketEvent<Announcement>>() {}.type
                val event = gson.fromJson<WebSocketEvent<Announcement>>(msg, type)

                when (event.type) {
                    WebSocketEventType.SYNC, WebSocketEventType.CREATED, WebSocketEventType.UPDATED -> {
                        val isNew = event.type == WebSocketEventType.CREATED || (event.type == WebSocketEventType.UPDATED && Announcements.announcement?.id != event.data?.id)
                        Announcements.announcement = event.data
                        RFULogger.dev("Announcement updated via WebSocket: ${Announcements.announcement?.title}")

                        if (isNew && event.data != null) {
                            cloud.glitchdev.rfu.utils.Chat.sendMessage(
                                event.data.message.toInteractiveText(
                                    "/rfuannouncement open",
                                    net.minecraft.network.chat.Component.literal("Open announcement")
                                )
                            )
                        }
                    }
                    WebSocketEventType.DELETED -> {
                        Announcements.announcement = null
                        RFULogger.dev("Announcement deleted via WebSocket")
                    }
                }
            } catch (e: Exception) {
                RFULogger.error("Error while parsing announcement websocket message: ", e)
            }
        }

        RFULogger.dev("Subscribing to announcement topics...")
        WebSocketClient.subscribe("/topic/announcements", callback)
        WebSocketClient.subscribe("/app/topic/announcements", callback)
    }
}
