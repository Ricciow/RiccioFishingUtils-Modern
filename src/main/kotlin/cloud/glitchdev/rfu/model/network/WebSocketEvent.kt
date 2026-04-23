package cloud.glitchdev.rfu.model.network

data class WebSocketEvent<T>(
    val type: WebSocketEventType,
    val data: T? = null,
    val id: String? = null
)
