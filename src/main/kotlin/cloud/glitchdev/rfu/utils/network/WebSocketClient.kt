package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.utils.RFULogger
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap

object WebSocketClient {
    private val client = HttpClient.newHttpClient()
    private var webSocket: WebSocket? = null
    private val subscriptions = ConcurrentHashMap<String, (String) -> Unit>()
    private val gson = Gson()
    private var lastAuthToken: String? = null
    private var reconnectAttempts = 0
    private var isReconnecting = false
    
    var isConnected = false
        private set

    fun connect(authToken: String) {
        lastAuthToken = authToken
        if (isConnected || isReconnecting) return
        
        val wsUrl = API_URL.replace("https://", "wss://").replace("http://", "ws://").replace("/api", "") + "/ws"
        RFULogger.dev("Connecting to WebSocket: $wsUrl")
        
        client.newWebSocketBuilder()
            .buildAsync(URI.create(wsUrl), object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    RFULogger.dev("WebSocket Opened (URL: $wsUrl), sending CONNECT frame...")
                    reconnectAttempts = 0
                    isReconnecting = false
                    sendFrame(webSocket, "CONNECT", mapOf(
                        "accept-version" to "1.1,1.2",
                        "heart-beat" to "10000,10000",
                        "Authorization" to authToken
                    ))
                    webSocket.request(1)
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                    val frame = data.toString()
                    if (frame.isBlank()) {
                        // Respond to server heartbeat
                        webSocket.sendText("\n", true)
                    } else {
                        RFULogger.dev("Received WebSocket frame: ${frame.take(100)}...")
                        handleFrame(frame)
                    }
                    webSocket.request(1)
                    return null
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
                    RFULogger.info("WebSocket Closed: $statusCode $reason")
                    isConnected = false
                    if (statusCode != WebSocket.NORMAL_CLOSURE) {
                        attemptReconnect()
                    }
                    return null
                }

                override fun onError(webSocket: WebSocket, error: Throwable) {
                    RFULogger.error("WebSocket Error (on Listener): ", error)
                    isConnected = false
                    attemptReconnect()
                }
            }).thenAccept { ws ->
                RFULogger.dev("WebSocket buildAsync successful")
                webSocket = ws
            }.exceptionally { error ->
                RFULogger.error("WebSocket buildAsync failed: ", error)
                isReconnecting = false
                attemptReconnect()
                null
            }
    }

    private fun attemptReconnect() {
        if (isConnected || isReconnecting || lastAuthToken == null) return
        isReconnecting = true
        
        reconnectAttempts++
        val delay = (minOf(reconnectAttempts * 5, 60)).toLong()
        RFULogger.info("Attempting to reconnect WebSocket in $delay seconds (attempt $reconnectAttempts)...")
        
        CompletableFuture.delayedExecutor(delay, java.util.concurrent.TimeUnit.SECONDS).execute {
            isReconnecting = false
            lastAuthToken?.let { connect(it) }
        }
    }

    private fun handleFrame(frameData: String) {
        val lines = frameData.split("\n")
        if (lines.isEmpty() || lines[0].isBlank()) return
        
        val command = lines[0].trim()
        RFULogger.dev("Handling STOMP command: $command")
        if (command == "CONNECTED") {
            isConnected = true
            RFULogger.info("STOMP Connected")
            subscriptions.forEach { (topic, _) ->
                RFULogger.dev("Re-subscribing to $topic after reconnection")
                subscribe(topic)
            }
        } else if (command == "MESSAGE") {
            val headers = mutableMapOf<String, String>()
            var bodyIndex = -1
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) {
                    bodyIndex = i + 1
                    break
                }
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    headers[parts[0]] = parts[1]
                }
            }
            
            if (bodyIndex != -1 && bodyIndex < lines.size) {
                val body = lines.subList(bodyIndex, lines.size).joinToString("\n").trimEnd { it == '\u0000' }
                val destination = headers["destination"]
                RFULogger.dev("STOMP Message for destination: $destination")
                if (destination != null) {
                    if (subscriptions.containsKey(destination)) {
                        subscriptions[destination]?.invoke(body)
                    } else {
                        RFULogger.dev("No subscription found for destination: $destination")
                    }
                }
            }
        } else if (command == "ERROR") {
            RFULogger.error("Received STOMP ERROR frame: $frameData")
        } else {
            RFULogger.dev("Unhandled STOMP command: $command")
        }
    }

    fun subscribe(topic: String, callback: ((String) -> Unit)? = null) {
        if (callback != null) {
            subscriptions[topic] = callback
        }
        
        if (isConnected) {
            RFULogger.dev("Sending SUBSCRIBE frame for $topic")
            sendFrame(webSocket!!, "SUBSCRIBE", mapOf(
                "id" to "sub-${topic.hashCode()}",
                "destination" to topic
            ))
        } else {
            RFULogger.dev("Queued subscription for $topic (not connected yet)")
        }
    }

    fun send(destination: String, payload: Any) {
        if (isConnected) {
            val body = if (payload is String) payload else gson.toJson(payload)
            sendFrame(webSocket!!, "SEND", mapOf(
                "destination" to destination,
                "content-type" to "application/json"
            ), body)
        }
    }

    private fun sendFrame(ws: WebSocket, command: String, headers: Map<String, String>, body: String = "") {
        val frame = StringBuilder()
        frame.append(command).append("\n")
        headers.forEach { (k, v) ->
            frame.append(k).append(":").append(v).append("\n")
        }
        frame.append("\n")
        frame.append(body)
        frame.append("\u0000")
        
        ws.sendText(frame.toString(), true)
    }

    fun disconnect() {
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting")
        isConnected = false
    }
}
