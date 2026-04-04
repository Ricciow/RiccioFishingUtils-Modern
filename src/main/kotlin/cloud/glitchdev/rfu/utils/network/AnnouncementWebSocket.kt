package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.AnnouncementEvents
import cloud.glitchdev.rfu.feature.announcements.Announcements
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.reflect.TypeToken

@AutoRegister
object AnnouncementWebSocket : RegisteredEvent {

    override fun register() {
        RFULogger.dev("Registering AnnouncementWebSocket")
        val callback : (String) -> Unit = { msg ->
            try {
                val type = object : TypeToken<WebSocketEvent<Announcement>>() {}.type
                val event = Announcements.gson.fromJson<WebSocketEvent<Announcement>>(msg, type)

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
