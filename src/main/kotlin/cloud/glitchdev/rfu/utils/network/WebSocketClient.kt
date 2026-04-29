package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.RFU_VERSION
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.events.managers.ErrorEvents
import cloud.glitchdev.rfu.events.managers.WebSocketEvents
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import kotlin.time.Clock
import kotlin.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

object WebSocketClient {
    private val client = HttpClient.newHttpClient()
    private val USER_AGENT = "Java-http-client/${System.getProperty("java.version")} rfu:${RFU_VERSION.friendlyString}"
    private var webSocket: WebSocket? = null
    private val subscriptions = ConcurrentHashMap<String, (String) -> Unit>()
    private val gson = Gson()
    private var lastAuthToken: String? = null
    private var reconnectAttempts = 0
    private var isReconnecting = false
    
    var isConnected = false
        private set(value) {
            if (field != value) {
                field = value
                WebSocketEvents.trigger(value)
            }
        }

    var lastIncomingTime: Instant? = null
        private set

    fun connect(authToken: String) {
        lastAuthToken = authToken
        if (isConnected || isReconnecting) return
        
        val wsUrl = API_URL.replace("https://", "wss://").replace("http://", "ws://").replace("/api", "") + "/ws"
        RFULogger.dev("Connecting to WebSocket: $wsUrl")
        
        client.newWebSocketBuilder()
            .header("User-Agent", USER_AGENT)
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
                    lastIncomingTime = Clock.System.now()
                    val frame = data.toString()
                    if (frame.trim() == "") {
                        // Respond to server heartbeat
                        webSocket.sendText("\n", true)
                    } else {
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
        val baseDelay = minOf(2.0.pow(reconnectAttempts), 60.0).toLong()
        val jitter = (Math.random() * 5).toLong()
        val delay = baseDelay + jitter
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
        
        val body = if (bodyIndex != -1 && bodyIndex < lines.size) {
            lines.subList(bodyIndex, lines.size).joinToString("\n").trimEnd { it == '\u0000' }
        } else ""

        RFULogger.dev("STOMP:\n--- Incoming STOMP Frame ---\n" +
                "Command: $command\n" +
                "Headers: $headers\n" +
                "Body: ${if (body.isEmpty()) "(empty)" else body}\n" +
                "---------------------------")

        if (command == "CONNECTED") {
            isConnected = true
            RFULogger.info("STOMP Connected")

            subscribe("/user/queue/errors") { msg ->
                try {
                    val map = gson.fromJson(msg, Map::class.java)
                    val errorMsg = map["message"]?.toString() ?: "Unknown error"
                    val origin = map["origin"]?.toString() ?: "UNKNOWN"
                    ErrorEvents.trigger(errorMsg, origin)
                } catch (e: Exception) {
                    RFULogger.error("Error parsing backend error message: ", e)
                }
            }

            subscriptions.forEach { (topic, _) ->
                RFULogger.dev("Re-subscribing to $topic after reconnection")
                subscribe(topic)
            }
        } else if (command == "MESSAGE") {
            val destination = headers["destination"]
            if (destination != null) {
                if (subscriptions.containsKey(destination)) {
                    subscriptions[destination]?.invoke(body)
                } else {
                    RFULogger.dev("No subscription found for destination: $destination")
                }
            }
        } else if (command == "ERROR") {
            RFULogger.error("Received STOMP ERROR frame:\nHeaders: $headers\nBody: $body")
        } else {
            RFULogger.dev("Unhandled STOMP command: $command")
        }
    }

    fun subscribe(topic: String, callback: ((String) -> Unit)? = null) {
        if (callback != null) {
            subscriptions[topic] = callback
        }
        
        if (isConnected) {
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
        
        RFULogger.dev("STOMP:\n---- Outgoing STOMP Frame ---\n" +
                "Command: $command\n" +
                "Headers: $headers\n" +
                "Body: ${if (body.isEmpty()) "(empty)" else body}\n" +
                "---------------------------")
        
        try {
            ws.sendText(frame.toString(), true)
        } catch (e: Exception) {
            RFULogger.error("Error sending STOMP frame: ", e)
        }
    }

    fun disconnect() {
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnecting")
        webSocket = null
        isConnected = false
        isReconnecting = false
        lastAuthToken = null
    }
}
