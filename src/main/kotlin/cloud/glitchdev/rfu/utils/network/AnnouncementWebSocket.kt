package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.AnnouncementEvents
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.utils.RFULogger
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
                val type = object : TypeToken<WebSocketEvent<Announcement>>() {}.type
                val event = gson.fromJson<WebSocketEvent<Announcement>>(msg, type)

                when (event.type) {
                    WebSocketEventType.SYNC, WebSocketEventType.CREATED, WebSocketEventType.UPDATED -> {
                        RFULogger.dev("Announcement update received via WebSocket: ${event.data?.title}")
                        AnnouncementEvents.trigger(event.data)
                    }
                    WebSocketEventType.DELETED -> {
                        RFULogger.dev("Announcement deletion received via WebSocket")
                        AnnouncementEvents.trigger(null)
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
